package br.com.mutua.createchhub;

import java.util.UUID;

import br.com.mutua.createchhub.support.PostgresTestConfiguration;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Import(PostgresTestConfiguration.class)
class DatabaseMigrationTests {

	@Autowired
	private Flyway flyway;

	@Autowired
	private JdbcTemplate jdbc;

	@Test
	void migrationIsAppliedOnStartupAndIsNotRepeated() {
		flyway.validate();

		assertThat(flyway.info().current().getVersion().toString()).isEqualTo("2");
		assertThat(flyway.migrate().migrationsExecuted).isZero();
		assertThat(flyway.info().applied()).hasSize(2);
	}

	@Test
	void upgradesLegacyRolesWithoutGrantingNewAdministrativeAccess() {
		migrations("legacy_roles", "1").migrate();
		for (var role : new String[] { "ADMIN", "REQUESTER", "OPERATOR", "BOARD", "STAFF" }) {
			insertLegacyAccount("legacy_roles", role + "@EXAMPLE.COM", role);
		}

		migrations("legacy_roles", "latest").migrate();

		assertThat(jdbc.queryForObject("SELECT access_role FROM legacy_roles.user_accounts WHERE email = ?",
				String.class, "admin@example.com")).isEqualTo("ADMIN");
		assertThat(jdbc.queryForObject("SELECT count(*) FROM legacy_roles.user_accounts WHERE access_role = 'USER'",
				Integer.class)).isEqualTo(4);
	}

	@Test
	void refusesDuplicateLoginEmailsWithoutDeletingOrChangingLegacyAccounts() {
		migrations("duplicate_emails", "1").migrate();
		insertLegacyAccount("duplicate_emails", "same@example.com", "REQUESTER");
		insertLegacyAccount("duplicate_emails", "SAME@example.com", "STAFF");

		assertThatThrownBy(() -> migrations("duplicate_emails", "latest").migrate())
				.isInstanceOf(FlywayException.class);
		assertThat(jdbc.queryForObject("SELECT count(*) FROM duplicate_emails.user_accounts", Integer.class))
				.isEqualTo(2);
		assertThat(jdbc.queryForObject("SELECT count(*) FROM duplicate_emails.user_accounts WHERE access_role = 'USER'",
				Integer.class)).isZero();
	}

	private Flyway migrations(String schema, String target) {
		return Flyway.configure().dataSource(flyway.getConfiguration().getDataSource())
				.schemas(schema).defaultSchema(schema).target(target).load();
	}

	private void insertLegacyAccount(String schema, String email, String role) {
		jdbc.update("INSERT INTO " + schema
				+ ".user_accounts (id, full_name, email, password_hash, access_role) VALUES (?, ?, ?, ?, ?)",
				UUID.randomUUID(), "Conta legada", email, "legacy-test-hash", role);
	}
}
