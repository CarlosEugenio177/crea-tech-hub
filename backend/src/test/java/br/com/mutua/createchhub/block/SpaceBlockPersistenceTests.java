package br.com.mutua.createchhub.block;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.mutua.createchhub.support.PostgresTestConfiguration;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Import(PostgresTestConfiguration.class)
@Transactional
class SpaceBlockPersistenceTests {

  @Autowired private JdbcTemplate jdbc;
  private UUID room;
  private UUID actor;

  @BeforeEach
  void prepare() {
    room = UUID.randomUUID();
    actor = UUID.randomUUID();
    jdbc.update(
        "INSERT INTO spaces (id, name, type, capacity) VALUES (?, 'Sala', 'ROOM', 1)", room);
    jdbc.update(
        "INSERT INTO user_accounts (id, full_name, email, password_hash, access_role) VALUES (?, 'Operador', 'blocks@example.com', 'test-hash', 'ADMIN')",
        actor);
  }

  @ParameterizedTest
  @CsvSource({
    "2027-03-10T10:00,2027-03-10T09:00,ck_space_blocks_interval",
    "2027-03-10T10:00,2027-03-10T10:00,ck_space_blocks_interval",
    "2027-03-10T09:00:30,2027-03-10T10:00,ck_space_blocks_minutes",
    "2027-03-10T09:00,2027-03-10T10:00:30,ck_space_blocks_minutes"
  })
  void rejectsInvalidIntervalsEvenWithoutHttpValidation(
      String start, String end, String constraint) {
    assertThatThrownBy(() -> insert(start, end, "Manutenção"))
        .isInstanceOf(DataIntegrityViolationException.class)
        .hasMessageContaining(constraint);
  }

  @Test
  void rejectsAnEmptyReason() {
    assertThatThrownBy(() -> insert("2027-03-10T09:00", "2027-03-10T10:00", "   "))
        .isInstanceOf(DataIntegrityViolationException.class)
        .hasMessageContaining("ck_space_blocks_reason");
  }

  @Test
  void requiresTheAdministratorWhenRecordingCancellation() {
    var id = insert("2027-03-10T09:00", "2027-03-10T10:00", "Manutenção");
    assertThatThrownBy(
            () ->
                jdbc.update(
                    "UPDATE space_blocks SET cancelled_at = CURRENT_TIMESTAMP WHERE id = ?", id))
        .isInstanceOf(DataIntegrityViolationException.class)
        .hasMessageContaining("ck_space_blocks_cancellation");
  }

  private UUID insert(String start, String end, String reason) {
    var id = UUID.randomUUID();
    jdbc.update(
        "INSERT INTO space_blocks (id, space_id, starts_at, ends_at, reason, created_by) VALUES (?, ?, ?::timestamp, ?::timestamp, ?, ?)",
        id,
        room,
        start,
        end,
        reason,
        actor);
    return id;
  }
}
