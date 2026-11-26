package br.com.mutua.createchhub.reservation;

import br.com.mutua.createchhub.space.SpaceAvailabilityService;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class AdminReservationQuery {
  private final NamedParameterJdbcOperations jdbc;

  private final Clock clock;

  public AdminReservationQuery(NamedParameterJdbcOperations jdbc, Clock clock) {
    this.jdbc = jdbc;
    this.clock = clock;
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
    var now = clock.instant();
    params.addValue("nowLocal", LocalDateTime.ofInstant(now, SpaceAvailabilityService.HUB_ZONE));
    params.addValue("now", now.atOffset(ZoneOffset.UTC));
    List<AdminReservationPage.Entry> items =
        jdbc.query(
            """
        SELECT id, space_name, station_number, starts_at, ends_at, status, full_name, email,
               whatsapp, reason, profile, notes, late_cancellation,
               no_show_at IS NOT NULL AS no_show,
               (status = 'CONFIRMED' AND ends_at <= :nowLocal) AS attendance_editable,
               (SELECT count(*) FROM reservations absences
                 WHERE absences.cpf = reservations.cpf AND absences.no_show_at IS NOT NULL) AS no_show_count,
               (SELECT max(ends_at) FROM requester_reservation_blocks
                 WHERE cpf = reservations.cpf AND revoked_at IS NULL AND ends_at > :now) AS blocked_until,
               (SELECT state FROM reservation_emails WHERE reservation_id = reservations.id
                 ORDER BY id DESC LIMIT 1) AS email_delivery_status
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
                    ReservationRequest.Profile.valueOf(row.getString("profile")),
                    row.getString("notes"),
                    row.getBoolean("late_cancellation"),
                    row.getBoolean("no_show"),
                    row.getBoolean("attendance_editable"),
                    row.getLong("no_show_count"),
                    Optional.ofNullable(row.getObject("blocked_until", OffsetDateTime.class))
                        .map(OffsetDateTime::toInstant)
                        .orElse(null),
                    deliveryStatus(row.getString("email_delivery_status"))));
    return new AdminReservationPage(
        items, page, size, total, Math.toIntExact(Math.ceilDiv(total, size)));
  }

  private static ReservationEmailQueue.DeliveryStatus deliveryStatus(String state) {
    return state == null ? null : ReservationEmailQueue.DeliveryStatus.valueOf(state);
  }

  private static boolean hasText(String value) {
    return value != null && !value.isBlank();
  }

  public record Filters(String search, String space, LocalDate date, ReservationStatus status) {}
}
