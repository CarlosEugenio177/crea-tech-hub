package br.com.mutua.createchhub.reservation;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
class ReservationEmailQueue {
  private final JdbcTemplate jdbc;
  private final boolean enabled;

  ReservationEmailQueue(JdbcTemplate jdbc, ReservationEmailSettings settings) {
    this.jdbc = jdbc;
    enabled = settings.enabled();
  }

  void enqueue(UUID reservationId, ReservationStatus status) {
    if (!enabled) return;
    jdbc.update(
        """
        INSERT INTO reservation_emails (reservation_id, event_status) VALUES (?, ?)
        ON CONFLICT (reservation_id, event_status) DO NOTHING
        """,
        reservationId,
        status.name());
  }

  void expireUnfinished() {
    jdbc.update(
        """
        UPDATE reservation_emails SET state = 'FAILED'
        WHERE state = 'QUEUED' AND available_at <= now()
          AND (attempts >= 4 OR created_at <= now() - interval '12 hours')
        """);
  }

  Optional<Message> claim() {
    return jdbc
        .query(
            """
        WITH next_email AS (
          SELECT e.id FROM reservation_emails e
          WHERE e.state = 'QUEUED' AND e.available_at <= now() AND e.attempts < 4
            AND e.created_at > now() - interval '12 hours'
            AND NOT EXISTS (
              SELECT 1 FROM reservation_emails previous
              WHERE previous.reservation_id = e.reservation_id AND previous.id < e.id
                AND previous.state = 'QUEUED'
            )
          ORDER BY e.available_at, e.id
          LIMIT 1 FOR UPDATE SKIP LOCKED
        )
        UPDATE reservation_emails e
        SET attempts = e.attempts + 1, available_at = now() + interval '2 minutes'
        FROM next_email, reservations r
        WHERE e.id = next_email.id AND r.id = e.reservation_id
        RETURNING e.id, e.attempts, e.event_status, r.id AS reservation_id, r.email, r.space_name,
          r.station_number, r.starts_at, r.ends_at
        """,
            (row, index) ->
                new Message(
                    row.getLong("id"),
                    row.getInt("attempts"),
                    ReservationStatus.valueOf(row.getString("event_status")),
                    row.getObject("reservation_id", UUID.class),
                    row.getString("email"),
                    row.getString("space_name"),
                    row.getObject("station_number", Integer.class),
                    row.getObject("starts_at", LocalDateTime.class),
                    row.getObject("ends_at", LocalDateTime.class)))
        .stream()
        .findFirst();
  }

  boolean attachLink(UUID id, String hash) {
    return jdbc.update(
            """
        UPDATE reservations SET email_tracking_hash = ?
        WHERE id = ? AND (email_tracking_hash IS NULL OR email_tracking_hash = ?)
        """,
            hash,
            id,
            hash)
        == 1;
  }

  void complete(Message message, DeliveryStatus status) {
    jdbc.update(
        """
        UPDATE reservation_emails SET state = ?, available_at = now() + interval '1 minute'
        WHERE id = ? AND attempts = ? AND state = 'QUEUED'
        """,
        status.name(),
        message.id(),
        message.attempt());
  }

  @Schema(name = "ReservationEmailDeliveryStatus", enumAsRef = true)
  public enum DeliveryStatus {
    QUEUED,
    ACCEPTED,
    FAILED
  }

  record Message(
      long id,
      int attempt,
      ReservationStatus status,
      UUID reservationId,
      String recipient,
      String spaceName,
      Integer stationNumber,
      LocalDateTime startsAt,
      LocalDateTime endsAt) {}
}
