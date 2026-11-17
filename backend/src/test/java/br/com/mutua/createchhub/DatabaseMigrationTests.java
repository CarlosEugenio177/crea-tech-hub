package br.com.mutua.createchhub;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.mutua.createchhub.support.PostgresTestConfiguration;
import java.util.UUID;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
@Import(PostgresTestConfiguration.class)
class DatabaseMigrationTests {

  @Autowired private Flyway flyway;

  @Autowired private JdbcTemplate jdbc;

  @Test
  void migrationIsAppliedOnStartupAndIsNotRepeated() {
    flyway.validate();

    assertThat(flyway.info().current().getVersion().toString()).isEqualTo("8");
    assertThat(flyway.migrate().migrationsExecuted).isZero();
    assertThat(flyway.info().applied()).hasSize(8);
  }

  @Test
  void addsSpacesToAnExistingDatabaseWithoutChangingAccounts() {
    migrations("spaces_upgrade", "2").migrate();
    insertLegacyAccount("spaces_upgrade", "admin@example.com", "ADMIN");

    migrations("spaces_upgrade", "latest").migrate();

    assertThat(jdbc.queryForObject("SELECT count(*) FROM spaces_upgrade.spaces", Integer.class))
        .isZero();
    assertThat(
            jdbc.queryForObject(
                "SELECT password_hash FROM spaces_upgrade.user_accounts WHERE email = ?",
                String.class,
                "admin@example.com"))
        .isEqualTo("legacy-test-hash");
  }

  @Test
  void migratesExistingAdministrativePermissionsWithoutChangingRolesOrPasswords() {
    migrations("permissions_upgrade", "3").migrate();
    insertLegacyAccount("permissions_upgrade", "admin@example.com", "ADMIN");
    insertLegacyAccount("permissions_upgrade", "user@example.com", "USER");

    migrations("permissions_upgrade", "latest").migrate();

    assertThat(
            jdbc.queryForList(
                "SELECT permission FROM permissions_upgrade.user_account_permissions p "
                    + "JOIN permissions_upgrade.user_accounts a ON a.id = p.account_id WHERE a.email = ?",
                String.class,
                "admin@example.com"))
        .containsExactlyInAnyOrder("MANAGE_ACCESS", "MANAGE_SPACES");
    assertThat(
            jdbc.queryForObject(
                "SELECT count(*) FROM permissions_upgrade.user_account_permissions p "
                    + "JOIN permissions_upgrade.user_accounts a ON a.id = p.account_id WHERE a.access_role = 'USER'",
                Integer.class))
        .isZero();
    assertThat(
            jdbc.queryForObject(
                "SELECT password_hash FROM permissions_upgrade.user_accounts WHERE email = ?",
                String.class,
                "admin@example.com"))
        .isEqualTo("legacy-test-hash");
  }

  @Test
  void initializesHubHoursWhenUpgradingWithoutChangingExistingAccounts() {
    migrations("hours_upgrade", "4").migrate();
    insertLegacyAccount("hours_upgrade", "admin@example.com", "ADMIN");

    migrations("hours_upgrade", "latest").migrate();

    assertThat(
            jdbc.queryForObject(
                "SELECT opening_time::text FROM hours_upgrade.hub_operating_hours WHERE id = 1",
                String.class))
        .isEqualTo("08:00:00");
    assertThat(
            jdbc.queryForObject(
                "SELECT closing_time::text FROM hours_upgrade.hub_operating_hours WHERE id = 1",
                String.class))
        .isEqualTo("18:00:00");
    assertThat(
            jdbc.queryForList(
                "SELECT day_of_week FROM hours_upgrade.hub_operating_days", String.class))
        .containsExactlyInAnyOrder("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY");
    assertThat(
            jdbc.queryForObject(
                "SELECT password_hash FROM hours_upgrade.user_accounts WHERE email = ?",
                String.class,
                "admin@example.com"))
        .isEqualTo("legacy-test-hash");
  }

  @Test
  void preservesExistingSpacesWithoutInventingStationPositions() {
    migrations("layout_upgrade", "5").migrate();
    var id = UUID.randomUUID();
    jdbc.update(
        "INSERT INTO layout_upgrade.spaces (id, name, type, capacity, active) VALUES (?, 'Legada', 'WORKSTATION', 1, false)",
        id);

    migrations("layout_upgrade", "latest").migrate();

    assertThat(
            jdbc.queryForMap(
                "SELECT name, type, capacity, active FROM layout_upgrade.spaces WHERE id = ?", id))
        .containsEntry("name", "Legada")
        .containsEntry("type", "WORKSTATION")
        .containsEntry("capacity", 1)
        .containsEntry("active", false);
    assertThat(
            jdbc.queryForObject(
                "SELECT room_id FROM layout_upgrade.spaces WHERE id = ?", UUID.class, id))
        .isNull();
    assertThat(
            jdbc.queryForObject(
                "SELECT station_number FROM layout_upgrade.spaces WHERE id = ?", Integer.class, id))
        .isNull();
  }

  @Test
  void addsBlocksWithoutChangingExistingSpacesOrAccounts() {
    migrations("blocks_upgrade", "6").migrate();
    insertLegacyAccount("blocks_upgrade", "admin@example.com", "ADMIN");
    var id = UUID.randomUUID();
    jdbc.update(
        "INSERT INTO blocks_upgrade.spaces (id, name, type, capacity) VALUES (?, 'Sala preservada', 'ROOM', 4)",
        id);

    migrations("blocks_upgrade", "latest").migrate();

    assertThat(
            jdbc.queryForObject("SELECT count(*) FROM blocks_upgrade.space_blocks", Integer.class))
        .isZero();
    assertThat(
            jdbc.queryForObject(
                "SELECT name FROM blocks_upgrade.spaces WHERE id = ?", String.class, id))
        .isEqualTo("Sala preservada");
    assertThat(
            jdbc.queryForObject(
                "SELECT password_hash FROM blocks_upgrade.user_accounts WHERE email = ?",
                String.class,
                "admin@example.com"))
        .isEqualTo("legacy-test-hash");
  }

  @Test
  void infersStationBookingModeWithoutChangingExistingStationIdentities() {
    migrations("modes_upgrade", "7").migrate();
    var room = UUID.randomUUID();
    var station = UUID.randomUUID();
    jdbc.update(
        "INSERT INTO modes_upgrade.spaces (id, name, type, capacity) VALUES (?, 'Coworking', 'ROOM', 16)",
        room);
    jdbc.update(
        "INSERT INTO modes_upgrade.spaces (id, name, type, capacity, room_id, station_number, map_x, map_y) VALUES (?, 'Estação 1', 'WORKSTATION', 1, ?, 1, 25, 50)",
        station,
        room);

    migrations("modes_upgrade", "latest").migrate();

    assertThat(
            jdbc.queryForObject(
                "SELECT booking_mode FROM modes_upgrade.spaces WHERE id = ?", String.class, room))
        .isEqualTo("PER_STATION");
    assertThat(
            jdbc.queryForObject(
                "SELECT room_id FROM modes_upgrade.spaces WHERE id = ?", UUID.class, station))
        .isEqualTo(room);
    assertThat(
            jdbc.queryForObject(
                "SELECT station_number FROM modes_upgrade.spaces WHERE id = ?",
                Integer.class,
                station))
        .isEqualTo(1);
    assertThat(
            jdbc.queryForObject(
                "SELECT version FROM modes_upgrade.spaces WHERE id = ?", Long.class, station))
        .isZero();
  }

  @Test
  void upgradesLegacyRolesWithoutGrantingNewAdministrativeAccess() {
    migrations("legacy_roles", "1").migrate();
    for (var role : new String[] {"ADMIN", "REQUESTER", "OPERATOR", "BOARD", "STAFF"}) {
      insertLegacyAccount("legacy_roles", role + "@EXAMPLE.COM", role);
    }

    migrations("legacy_roles", "latest").migrate();

    assertThat(
            jdbc.queryForObject(
                "SELECT access_role FROM legacy_roles.user_accounts WHERE email = ?",
                String.class,
                "admin@example.com"))
        .isEqualTo("ADMIN");
    assertThat(
            jdbc.queryForObject(
                "SELECT count(*) FROM legacy_roles.user_accounts WHERE access_role = 'USER'",
                Integer.class))
        .isEqualTo(4);
  }

  @Test
  void refusesDuplicateLoginEmailsWithoutDeletingOrChangingLegacyAccounts() {
    migrations("duplicate_emails", "1").migrate();
    insertLegacyAccount("duplicate_emails", "same@example.com", "REQUESTER");
    insertLegacyAccount("duplicate_emails", "SAME@example.com", "STAFF");

    assertThatThrownBy(() -> migrations("duplicate_emails", "latest").migrate())
        .isInstanceOf(FlywayException.class);
    assertThat(
            jdbc.queryForObject(
                "SELECT count(*) FROM duplicate_emails.user_accounts", Integer.class))
        .isEqualTo(2);
    assertThat(
            jdbc.queryForObject(
                "SELECT count(*) FROM duplicate_emails.user_accounts WHERE access_role = 'USER'",
                Integer.class))
        .isZero();
  }

  private Flyway migrations(String schema, String target) {
    return Flyway.configure()
        .dataSource(flyway.getConfiguration().getDataSource())
        .schemas(schema)
        .defaultSchema(schema)
        .target(target)
        .load();
  }

  private void insertLegacyAccount(String schema, String email, String role) {
    jdbc.update(
        "INSERT INTO "
            + schema
            + ".user_accounts (id, full_name, email, password_hash, access_role) VALUES (?, ?, ?, ?, ?)",
        UUID.randomUUID(),
        "Conta legada",
        email,
        "legacy-test-hash",
        role);
  }
}
