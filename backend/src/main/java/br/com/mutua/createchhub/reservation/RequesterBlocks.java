package br.com.mutua.createchhub.reservation;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.stereotype.Repository;

@Repository
class RequesterBlocks {
  static final int MIN_ABSENCES = 2;

  private final NamedParameterJdbcOperations jdbc;

  RequesterBlocks(NamedParameterJdbcOperations jdbc) {
    this.jdbc = jdbc;
  }

  void lock(String cpf) {
    jdbc.query(
        "SELECT pg_advisory_xact_lock(hashtextextended(:cpf, 1846317))",
        Map.of("cpf", cpf),
        rows -> null);
  }

  long absenceCount(String cpf) {
    return jdbc.queryForObject(
        "SELECT count(*) FROM reservations WHERE cpf = :cpf AND no_show_at IS NOT NULL",
        Map.of("cpf", cpf),
        Long.class);
  }

  Optional<Instant> activeUntil(String cpf, Instant now) {
    return jdbc
        .query(
            """
        SELECT ends_at FROM requester_reservation_blocks
        WHERE cpf = :cpf AND revoked_at IS NULL AND ends_at > :now
        ORDER BY ends_at DESC LIMIT 1
        """,
            Map.of("cpf", cpf, "now", utc(now)),
            (row, index) -> row.getObject("ends_at", OffsetDateTime.class).toInstant())
        .stream()
        .findFirst();
  }

  void replace(String cpf, UUID adminId, Instant now, Instant until) {
    revoke(cpf, adminId, now);
    jdbc.update(
        """
        INSERT INTO requester_reservation_blocks (cpf, starts_at, ends_at, decided_by)
        VALUES (:cpf, :now, :until, :adminId)
        """,
        Map.of("cpf", cpf, "now", utc(now), "until", utc(until), "adminId", adminId));
  }

  void revoke(String cpf, UUID adminId, Instant now) {
    jdbc.update(
        """
        UPDATE requester_reservation_blocks SET revoked_at = :now, revoked_by = :adminId
        WHERE cpf = :cpf AND revoked_at IS NULL AND ends_at > :now
        """,
        Map.of("cpf", cpf, "now", utc(now), "adminId", adminId));
  }

  private static OffsetDateTime utc(Instant time) {
    return time.atOffset(ZoneOffset.UTC);
  }
}
