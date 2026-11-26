package br.com.mutua.createchhub.reservation;

import br.com.mutua.createchhub.shared.error.ResourceNotFoundException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.stereotype.Repository;

@Repository
public class ReservationRepository {
  private final NamedParameterJdbcOperations jdbc;

  public ReservationRepository(NamedParameterJdbcOperations jdbc) {
    this.jdbc = jdbc;
  }

  public Set<UUID> occupied(List<UUID> ids, LocalDateTime start, LocalDateTime end) {
    return Set.copyOf(
        jdbc.queryForList(
            """
        SELECT DISTINCT space_id FROM reservations
        WHERE space_id IN (:ids) AND status IN ('PENDING', 'CONFIRMED')
          AND starts_at < :end AND ends_at > :start
        """,
            Map.of("ids", ids, "start", start, "end", end),
            UUID.class));
  }

  public boolean hasConflictForBlock(UUID spaceId, LocalDateTime start, LocalDateTime end) {
    return Boolean.TRUE.equals(
        jdbc.queryForObject(
            """
        SELECT EXISTS (SELECT 1 FROM reservations
          WHERE status IN ('PENDING', 'CONFIRMED') AND starts_at < :end AND ends_at > :start
            AND (space_id = :spaceId OR room_id = :spaceId OR space_id IN (
              SELECT id FROM spaces WHERE room_id = :spaceId
            )))
        """,
            Map.of("spaceId", spaceId, "start", start, "end", end),
            Boolean.class));
  }

  void persistPending(
      ReservationReceipt receipt,
      UUID spaceId,
      UUID roomId,
      ReservationRequest.Requester requester,
      String trackingHash,
      Instant acceptedAt) {
    var params =
        new MapSqlParameterSource()
            .addValue("id", receipt.protocol())
            .addValue("trackingHash", trackingHash)
            .addValue("spaceId", spaceId)
            .addValue("roomId", roomId)
            .addValue("spaceName", receipt.spaceName())
            .addValue("number", receipt.stationNumber())
            .addValue("start", receipt.startsAt())
            .addValue("end", receipt.endsAt())
            .addValue("cpf", requester.cpf())
            .addValue("name", requester.fullName().strip())
            .addValue("email", requester.email())
            .addValue("whatsapp", requester.whatsapp())
            .addValue("profile", requester.profile().name())
            .addValue("onBehalf", requester.onBehalfOfOthers())
            .addValue("reason", requester.reason().strip())
            .addValue("notes", requester.notes().strip())
            .addValue("acceptedAt", OffsetDateTime.ofInstant(acceptedAt, ZoneOffset.UTC));
    jdbc.update(
        """
        INSERT INTO reservations (id, tracking_hash, space_id, room_id, space_name, station_number,
          starts_at, ends_at, cpf, full_name, email, whatsapp, profile, on_behalf_of_others,
          reason, notes, status, rules_accepted_at)
        VALUES (:id, :trackingHash, :spaceId, :roomId, :spaceName, :number, :start, :end, :cpf,
          :name, :email, :whatsapp, :profile, :onBehalf, :reason, :notes, 'PENDING', :acceptedAt)
        """,
        params);
  }

  Optional<StoredReservation> findByTrackingHash(String hash) {
    return queryByHash(hash, false);
  }

  Optional<StoredReservation> findByTrackingHashForUpdate(String hash) {
    return queryByHash(hash, true);
  }

  void markCancelled(UUID id, Instant cancelledAt, boolean lateCancellation) {
    jdbc.update(
        """
        UPDATE reservations
        SET status = 'CANCELLED', cancelled_at = :cancelledAt, late_cancellation = :lateCancellation
        WHERE id = :id
        """,
        new MapSqlParameterSource()
            .addValue("id", id)
            .addValue("cancelledAt", OffsetDateTime.ofInstant(cancelledAt, ZoneOffset.UTC))
            .addValue("lateCancellation", lateCancellation));
  }

  String requesterCpf(UUID id) {
    return jdbc
        .queryForList("SELECT cpf FROM reservations WHERE id = :id", Map.of("id", id), String.class)
        .stream()
        .findFirst()
        .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado."));
  }

  Optional<AdminState> findAdminStateForUpdate(UUID id) {
    return jdbc
        .query(
            "SELECT status, ends_at, no_show_at IS NOT NULL AS no_show FROM reservations WHERE id = :id FOR UPDATE",
            Map.of("id", id),
            (row, index) ->
                new AdminState(
                    ReservationStatus.valueOf(row.getString("status")),
                    row.getObject("ends_at", LocalDateTime.class),
                    row.getBoolean("no_show")))
        .stream()
        .findFirst();
  }

  void recordNoShow(UUID id, UUID adminId, Instant recordedAt) {
    jdbc.update(
        "UPDATE reservations SET no_show_at = :at, no_show_recorded_by = :adminId WHERE id = :id",
        new MapSqlParameterSource()
            .addValue("id", id)
            .addValue("adminId", adminId)
            .addValue(
                "at",
                recordedAt == null ? null : OffsetDateTime.ofInstant(recordedAt, ZoneOffset.UTC)));
  }

  record AdminState(ReservationStatus status, LocalDateTime endsAt, boolean noShow) {}

  void recordDecision(UUID id, UUID adminId, ReservationStatus status, String note, Instant now) {
    var params =
        new MapSqlParameterSource()
            .addValue("id", id)
            .addValue("adminId", adminId)
            .addValue("status", status.name())
            .addValue("note", note)
            .addValue("now", OffsetDateTime.ofInstant(now, ZoneOffset.UTC));
    jdbc.update(
        """
        UPDATE reservations
        SET status = :status, cancelled_at = CASE WHEN :status = 'CANCELLED' THEN :now END
        WHERE id = :id
        """,
        params);
    jdbc.update(
        """
        INSERT INTO reservation_decisions (reservation_id, decided_by, status, note, decided_at)
        VALUES (:id, :adminId, :status, :note, :now)
        """,
        params);
  }

  private Optional<StoredReservation> queryByHash(String hash, boolean forUpdate) {
    var sql =
        """
        SELECT id, space_name, station_number, starts_at, ends_at, status, cancelled_at, late_cancellation,
          (SELECT note FROM reservation_decisions WHERE reservation_id = reservations.id
            ORDER BY id DESC LIMIT 1) AS decision_note
        FROM reservations WHERE tracking_hash = :hash OR email_tracking_hash = :hash
        """
            + (forUpdate ? " FOR UPDATE" : "");
    return jdbc
        .query(
            sql,
            Map.of("hash", hash),
            (row, index) -> {
              var cancelled = row.getObject("cancelled_at", OffsetDateTime.class);
              return new StoredReservation(
                  row.getObject("id", UUID.class),
                  row.getString("space_name"),
                  row.getObject("station_number", Integer.class),
                  row.getObject("starts_at", LocalDateTime.class),
                  row.getObject("ends_at", LocalDateTime.class),
                  ReservationStatus.valueOf(row.getString("status")),
                  cancelled == null ? null : cancelled.toInstant(),
                  row.getBoolean("late_cancellation"),
                  row.getString("decision_note"));
            })
        .stream()
        .findFirst();
  }

  record StoredReservation(
      UUID id,
      String spaceName,
      Integer stationNumber,
      LocalDateTime startsAt,
      LocalDateTime endsAt,
      ReservationStatus status,
      Instant cancelledAt,
      boolean lateCancellation,
      String decisionNote) {}
}
