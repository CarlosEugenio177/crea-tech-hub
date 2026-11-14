package br.com.mutua.createchhub.auth;

import java.util.Map;

import br.com.mutua.createchhub.support.PostgresTestConfiguration;
import br.com.mutua.createchhub.user.AccessRole;
import br.com.mutua.createchhub.user.UserAccount;
import br.com.mutua.createchhub.user.UserAccountRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(PostgresTestConfiguration.class)
class AdminAuthenticationTests {

	private static final String PASSWORD = "A-valid-test-password";

	@LocalServerPort
	private int port;

	@Autowired
	private UserAccountRepository repository;

	@Autowired
	private PasswordEncoder passwords;

	@AfterEach
	void removeTestAccounts() {
		repository.deleteAll();
	}

	@Test
	void authenticatesAdminRotatesSessionAndInvalidatesSessionOnLogout() throws Exception {
		var account = createAccount(AccessRole.ADMIN, true);
		try (var client = new SessionClient(port)) {
			var csrf = client.csrf();
			var previousSession = client.sessionId();
			var login = client.post("/api/v1/admin/auth/login",
					Map.of("email", " ADMIN@EXAMPLE.COM ", "password", PASSWORD), csrf);

			assertThat(login.statusCode()).isEqualTo(204);
			assertThat(client.sessionId()).isNotEqualTo(previousSession);
			assertThat(login.headers().allValues("Set-Cookie").toString()).contains("HttpOnly", "SameSite=Lax");
			var current = client.get("/api/v1/admin/auth/me");
			assertThat(current.statusCode()).isEqualTo(200);
			var body = JsonMapper.builder().build().readTree(current.body());
			assertThat(body.path("id").asString()).isEqualTo(account.getId().toString());
			assertThat(body.path("role").asString()).isEqualTo("ADMIN");
			assertThat(current.body()).doesNotContain("password", account.getPasswordHash());

			assertThat(client.post("/api/v1/admin/auth/logout", Map.of(), csrf).statusCode()).isEqualTo(403);
			assertThat(client.post("/api/v1/admin/auth/logout", Map.of(), client.csrf()).statusCode()).isEqualTo(204);
			assertThat(client.get("/api/v1/admin/auth/me").statusCode()).isEqualTo(401);
		}
	}

	@ParameterizedTest
	@ValueSource(strings = { "wrong-password", "missing", "inactive", "regular-user" })
	void rejectsInvalidAdministrativeCredentialsWithTheSameResponse(String scenario) throws Exception {
		if (!scenario.equals("missing")) {
			createAccount(scenario.equals("regular-user") ? AccessRole.USER : AccessRole.ADMIN,
					!scenario.equals("inactive"));
		}
		try (var client = new SessionClient(port)) {
			var response = client.post("/api/v1/admin/auth/login", Map.of("email", "admin@example.com",
					"password", scenario.equals("wrong-password") ? "wrong-password" : PASSWORD), client.csrf());
			assertThat(response.statusCode()).isEqualTo(401);
			assertThat(response.headers().firstValue("Content-Type").orElseThrow()).contains("application/problem+json");
			assertThat(JsonMapper.builder().build().readTree(response.body()).path("detail").asString())
					.isEqualTo("E-mail ou senha inválidos.");
			assertThat(client.get("/api/v1/admin/auth/me").statusCode()).isEqualTo(401);
		}
	}

	@ParameterizedTest
	@ValueSource(strings = { "login", "logout" })
	void rejectsAuthenticationWritesWithoutCsrf(String endpoint) throws Exception {
		try (var client = new SessionClient(port)) {
			var response = client.post("/api/v1/admin/auth/" + endpoint, Map.of(), null);
			assertThat(response.statusCode()).isEqualTo(403);
			assertThat(response.headers().firstValue("Content-Type").orElseThrow()).contains("application/problem+json");
		}
	}

	@Test
	void publicHealthAndCsrfDoNotRequireAnAccountAndRegistrationIsNotExposed() throws Exception {
		try (var client = new SessionClient(port)) {
			assertThat(client.get("/actuator/health").statusCode()).isEqualTo(200);
			assertThat(client.get("/api/v1/admin/auth/csrf").statusCode()).isEqualTo(200);
			assertThat(client.post("/api/v1/admin/auth/register", Map.of("role", "ADMIN"), client.csrf()).statusCode())
					.isEqualTo(401);
			assertThat(client.get("/api/v1/admin/auth/me").statusCode()).isEqualTo(401);
		}
	}

	private UserAccount createAccount(AccessRole role, boolean active) {
		var account = new UserAccount("Administrador de teste", "admin@example.com", passwords.encode(PASSWORD), role);
		if (!active) {
			account.deactivate();
		}
		return repository.saveAndFlush(account);
	}
}
