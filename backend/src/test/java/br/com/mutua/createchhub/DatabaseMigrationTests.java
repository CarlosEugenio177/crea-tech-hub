package br.com.mutua.createchhub;

import br.com.mutua.createchhub.support.PostgresTestConfiguration;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(PostgresTestConfiguration.class)
class DatabaseMigrationTests {

	@Autowired
	private Flyway flyway;

	@Test
	void migrationIsAppliedOnStartupAndIsNotRepeated() {
		flyway.validate();

		assertThat(flyway.info().current().getVersion().toString()).isEqualTo("1");
		assertThat(flyway.migrate().migrationsExecuted).isZero();
		assertThat(flyway.info().applied()).hasSize(1);
	}
}
