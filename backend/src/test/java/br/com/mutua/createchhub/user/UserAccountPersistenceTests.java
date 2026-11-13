package br.com.mutua.createchhub.user;

import java.util.UUID;

import br.com.mutua.createchhub.support.PostgresTestConfiguration;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Import(PostgresTestConfiguration.class)
@Transactional
class UserAccountPersistenceTests {

	private static final String PASSWORD_HASH = new BCryptPasswordEncoder().encode("test-password");

	@Autowired
	private UserAccountRepository repository;

	@Autowired
	private EntityManager entityManager;

	@Autowired
	private JdbcTemplate jdbc;

	@Test
	void persistsAccountAndLoadsItFromPostgres() {
		var saved = repository.saveAndFlush(
				new UserAccount("Ana Silva", "ana@example.com", PASSWORD_HASH, AccessRole.OPERATOR));
		entityManager.clear();

		var loaded = repository.findById(saved.getId()).orElseThrow();

		assertThat(loaded.getId()).isNotNull();
		assertThat(loaded.getFullName()).isEqualTo("Ana Silva");
		assertThat(loaded.getEmail()).isEqualTo("ana@example.com");
		assertThat(loaded.getPasswordHash()).isEqualTo(PASSWORD_HASH);
		assertThat(loaded.getAccessRole()).isEqualTo(AccessRole.OPERATOR);
		assertThat(loaded.isActive()).isTrue();
		assertThat(loaded.getCreatedAt()).isNotNull();
	}

	@Test
	void allowsRepeatedContactEmailAsDefinedInTheRequirements() {
		repository.save(new UserAccount("Ana Silva", "shared@example.com", PASSWORD_HASH, AccessRole.REQUESTER));
		repository.saveAndFlush(
				new UserAccount("Bruno Lima", "shared@example.com", PASSWORD_HASH, AccessRole.REQUESTER));
		entityManager.clear();

		assertThat(repository.count()).isEqualTo(2);
	}

	@Test
	void databaseRejectsAnUnknownAccessRole() {
		assertThatThrownBy(() -> jdbc.update("""
				INSERT INTO user_accounts (id, full_name, email, password_hash, access_role)
				VALUES (?, ?, ?, ?, ?)
				""", UUID.randomUUID(), "Ana Silva", "ana@example.com", PASSWORD_HASH, "UNKNOWN"))
				.isInstanceOf(DataIntegrityViolationException.class);
	}
}
