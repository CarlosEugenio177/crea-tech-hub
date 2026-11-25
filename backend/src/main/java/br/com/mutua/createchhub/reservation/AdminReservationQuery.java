package br.com.mutua.createchhub.reservation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class AdminReservationQuery {
  private final NamedParameterJdbcOperations jdbc;

  public AdminReservationQuery(NamedParameterJdbcOperations jdbc) {
    this.jdbc = jdbc;
  }

  @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
  public AdminReservationPage list(Filters filter, int page, int size) {
    var params =
        new MapSqlParameterSource().addValue("size", size).addValue("offset", (long) page * size);
    var conditions = new ArrayList<String>();
    if (hasText(filter.search())) {
      params.addValue("search", filter.search().strip());
      conditions.add("(strpos(lower(full_name), lower(:search)) > 0 OR id::text = lower(:search))");
    }
    if (hasText(filter.space())) {
      params.addValue("space", filter.space().strip());
      conditions.add("strpos(lower(space_name), lower(:space)) > 0");
    }
    if (filter.date() != null) {
      params.addValue("dayStart", filter.date().atStartOfDay());
      params.addValue("dayEnd", filter.date().plusDays(1).atStartOfDay());
      conditions.add("starts_at >= :dayStart AND starts_at < :dayEnd");
    }
    if (filter.status() != null) {
      params.addValue("status", filter.status().name());
      conditions.add("status = :status");
    }
    var from =
        conditions.isEmpty()
            ? " FROM reservations"
            : " FROM reservations WHERE " + String.join(" AND ", conditions);
    long total = jdbc.queryForObject("SELECT count(*)" + from, params, Long.class);
    List<AdminReservationPage.Entry> items =
        jdbc.query(
            """
        SELECT id, space_name, station_number, starts_at, ends_at, status, full_name, email,
               whatsapp, reason, notes, late_cancellation
        """
                + from
                + " ORDER BY starts_at DESC, id DESC LIMIT :size OFFSET :offset",
            params,
            (row, index) ->
                new AdminReservationPage.Entry(
                    row.getObject("id", UUID.class),
                    row.getString("space_name"),
                    row.getObject("station_number", Integer.class),
                    row.getObject("starts_at", LocalDateTime.class),
                    row.getObject("ends_at", LocalDateTime.class),
                    ReservationStatus.valueOf(row.getString("status")),
                    row.getString("full_name"),
                    row.getString("email"),
                    row.getString("whatsapp"),
                    row.getString("reason"),
                    row.getString("notes"),
                    row.getBoolean("late_cancellation")));
    return new AdminReservationPage(
        items, page, size, total, Math.toIntExact(Math.ceilDiv(total, size)));
  }

  private static boolean hasText(String value) {
    return value != null && !value.isBlank();
  }

  public record Filters(String search, String space, LocalDate date, ReservationStatus status) {}
}
