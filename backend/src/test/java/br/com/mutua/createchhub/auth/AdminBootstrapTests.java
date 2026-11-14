package br.com.mutua.createchhub.auth;

import br.com.mutua.createchhub.support.PostgresTestConfiguration;
import br.com.mutua.createchhub.user.AccessRole;
import br.com.mutua.createchhub.user.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
		"BOOTSTRAP_ADMIN_NAME=Administrador de teste",
		"BOOTSTRAP_ADMIN_EMAIL=FIRST@EXAMPLE.COM",
		"BOOTSTRAP_ADMIN_PASSWORD=Bootstrap-test-password"
})
@ActiveProfiles("bootstrap-admin")
@Import(PostgresTestConfiguration.class)
class AdminBootstrapTests {

	@Autowired
	private UserAccountRepository repository;

	@Autowired
	private PasswordEncoder passwords;

	@Autowired
	private AdminBootstrap bootstrap;

	@Test
	void createsOnlyTheInitialAdminAndNeverOverwritesItsPassword() {
		var account = repository.findByEmailAndAccessRole("first@example.com", AccessRole.ADMIN).orElseThrow();
		var originalHash = account.getPasswordHash();
		assertThat(originalHash).startsWith("{pbkdf2-v1}").doesNotContain("Bootstrap-test-password");
		assertThat(passwords.matches("Bootstrap-test-password", originalHash)).isTrue();

		bootstrap.run(null);

		assertThat(repository.count()).isEqualTo(1);
		assertThat(repository.findById(account.getId()).orElseThrow().getPasswordHash()).isEqualTo(originalHash);
	}
}
