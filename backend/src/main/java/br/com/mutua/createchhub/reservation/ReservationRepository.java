package br.com.mutua.createchhub.reservation;

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

  public boolean hasOccupiedStations(UUID room, LocalDateTime start, LocalDateTime end) {
    return Boolean.TRUE.equals(
        jdbc.queryForObject(
            """
        SELECT EXISTS (SELECT 1 FROM reservations WHERE room_id = :room
          AND status IN ('PENDING', 'CONFIRMED') AND starts_at < :end AND ends_at > :start)
        """,
            Map.of("room", room, "start", start, "end", end),
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

  Optional<ReservationReceipt> findReceipt(String hash) {
    return jdbc
        .query(
            """
        SELECT id, space_name, station_number, starts_at, ends_at, status
        FROM reservations WHERE tracking_hash = :hash
        """,
            Map.of("hash", hash),
            (row, index) ->
                new ReservationReceipt(
                    row.getObject("id", UUID.class),
                    row.getString("space_name"),
                    row.getObject("station_number", Integer.class),
                    row.getObject("starts_at", LocalDateTime.class),
                    row.getObject("ends_at", LocalDateTime.class),
                    row.getString("status")))
        .stream()
        .findFirst();
  }
}
