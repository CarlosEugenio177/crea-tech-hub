package br.com.mutua.createchhub.space;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.support.SqlArrayValue;
import org.springframework.stereotype.Repository;

@Repository
class SpaceBusyIntervals {
  private final NamedParameterJdbcOperations jdbc;

  SpaceBusyIntervals(NamedParameterJdbcOperations jdbc) {
    this.jdbc = jdbc;
  }

  List<Interval> find(
      List<UUID> ids, List<UUID> wholeRooms, LocalDateTime start, LocalDateTime end) {
    return jdbc.query(
        """
        WITH targets AS (
          SELECT CAST(:ids AS uuid[]) AS resources, CAST(:wholeRooms AS uuid[]) AS rooms
        )
        SELECT space_id AS resource_id, starts_at, ends_at FROM space_blocks CROSS JOIN targets
        WHERE space_id = ANY(resources) AND cancelled_at IS NULL
          AND starts_at < :end AND ends_at > :start
        UNION ALL
        SELECT CASE WHEN room_id = ANY(rooms) THEN room_id ELSE space_id END,
          starts_at, ends_at FROM reservations CROSS JOIN targets
        WHERE status IN ('PENDING', 'CONFIRMED') AND starts_at < :end AND ends_at > :start
          AND (space_id = ANY(resources) OR room_id = ANY(rooms))
        """,
        Map.of(
            "ids",
            new SqlArrayValue("uuid", ids.toArray()),
            "wholeRooms",
            new SqlArrayValue("uuid", wholeRooms.toArray()),
            "start",
            start,
            "end",
            end),
        (row, index) ->
            new Interval(
                row.getObject("resource_id", UUID.class),
                row.getObject("starts_at", LocalDateTime.class),
                row.getObject("ends_at", LocalDateTime.class)));
  }

  record Interval(UUID resourceId, LocalDateTime startsAt, LocalDateTime endsAt) {}
}
