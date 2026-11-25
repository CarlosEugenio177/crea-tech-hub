package br.com.mutua.createchhub.space;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.stereotype.Repository;

@Repository
class SpaceBusyIntervals {
  private final NamedParameterJdbcOperations jdbc;

  SpaceBusyIntervals(NamedParameterJdbcOperations jdbc) {
    this.jdbc = jdbc;
  }

  List<Interval> find(
      List<UUID> ids, UUID spaceId, boolean wholeRoom, LocalDateTime start, LocalDateTime end) {
    return jdbc.query(
        """
        SELECT space_id AS resource_id, starts_at, ends_at FROM space_blocks
        WHERE space_id IN (:ids) AND cancelled_at IS NULL AND starts_at < :end AND ends_at > :start
        UNION ALL
        SELECT CASE WHEN :wholeRoom AND room_id = :spaceId THEN :spaceId ELSE space_id END,
          starts_at, ends_at FROM reservations
        WHERE status IN ('PENDING', 'CONFIRMED') AND starts_at < :end AND ends_at > :start
          AND (space_id IN (:ids) OR (:wholeRoom AND room_id = :spaceId))
        """,
        Map.of("ids", ids, "spaceId", spaceId, "wholeRoom", wholeRoom, "start", start, "end", end),
        (row, index) ->
            new Interval(
                row.getObject("resource_id", UUID.class),
                row.getObject("starts_at", LocalDateTime.class),
                row.getObject("ends_at", LocalDateTime.class)));
  }

  record Interval(UUID resourceId, LocalDateTime startsAt, LocalDateTime endsAt) {}
}
