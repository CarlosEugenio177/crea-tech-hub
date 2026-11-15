package br.com.mutua.createchhub.recovery;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.mutua.createchhub.access.AccessAuditRepository;
import br.com.mutua.createchhub.shared.error.InvalidRequestException;
import br.com.mutua.createchhub.shared.error.ServiceUnavailableException;
import br.com.mutua.createchhub.support.PostgresTestConfiguration;
import br.com.mutua.createchhub.support.SessionClient;
import br.com.mutua.createchhub.user.AccessRole;
import br.com.mutua.createchhub.user.AdminPermission;
import br.com.mutua.createchhub.user.UserAccount;
import br.com.mutua.createchhub.user.UserAccountRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(PostgresTestConfiguration.class)
class PasswordRecoveryTests {

  private static final String PASSWORD = "Original-test-password";
  private static final String NEW_PASSWORD = "Recovered-test-password";
  private static final Instant NOW = Instant.parse("2026-11-15T15:00:00Z");
  @LocalServerPort private int port;
  @Autowired private UserAccountRepository accounts;
  @Autowired private PasswordResetTokenRepository tokens;
  @Autowired private PasswordResetService reset;
  @Autowired private AdminRecoveryService operational;
  @Autowired private PasswordEncoder passwords;
  @Autowired private AccessAuditRepository events;
  @MockitoBean private Clock clock;
  @MockitoBean private RecoveryMailSender mail;
  private UserAccount manager;

  @BeforeEach
  void prepare() {
    when(clock.instant()).thenReturn(NOW);
    manager =
        new UserAccount(
            "Gestor", "manager@example.com", passwords.encode(PASSWORD), AccessRole.ADMIN);
    manager.replacePermissions(EnumSet.allOf(AdminPermission.class));
    accounts.saveAndFlush(manager);
  }

  @AfterEach
  void cleanup() {
    events.deleteAll();
    tokens.deleteAll();
    accounts.deleteAll();
  }

  @Test
  void storesOnlyTheTokenHashAndResetsTheFirstAdminOnceInvalidatingSessions() throws Exception {
    try (var admin = new SessionClient(port);
        var visitor = new SessionClient(port)) {
      assertThat(
              admin
                  .post(
                      "/api/v1/admin/auth/login",
                      Map.of("email", manager.getEmail(), "password", PASSWORD),
                      admin.csrf())
                  .statusCode())
          .isEqualTo(204);
      var issued = reset.issue(manager.getEmail()).orElseThrow();
      var stored = tokens.findById(manager.getId()).orElseThrow();
      assertThat(stored.getTokenHash()).hasSize(64).isNotEqualTo(issued.token());
      assertThat(stored.getExpiresAt()).isEqualTo(NOW.plusSeconds(900));
      var body = Map.of("token", issued.token(), "newPassword", NEW_PASSWORD);
      assertThat(
              visitor
                  .json("POST", "/api/v1/admin/auth/password/reset", body, visitor.csrf())
                  .statusCode())
          .isEqualTo(204);
      assertThat(admin.get("/api/v1/admin/auth/me").statusCode()).isEqualTo(401);
      assertThat(
              visitor
                  .json("POST", "/api/v1/admin/auth/password/reset", body, visitor.csrf())
                  .statusCode())
          .isEqualTo(400);
      assertThat(
              passwords.matches(
                  NEW_PASSWORD, accounts.findById(manager.getId()).orElseThrow().getPasswordHash()))
          .isTrue();
      assertThat(events.findAll()).extracting(e -> e.getAction()).contains("PASSWORD_RESET");
    }
  }

  @Test
  void expiredLinksAndLinksIssuedBeforeAccessChangesCannotResetPasswords() {
    var grant = reset.issue(manager.getEmail()).orElseThrow();
    when(clock.instant()).thenReturn(NOW.plusSeconds(900));
    assertThatThrownBy(() -> reset.reset(new PasswordResetRequest(grant.token(), NEW_PASSWORD)))
        .isInstanceOf(InvalidRequestException.class);
    when(clock.instant()).thenReturn(NOW);
    manager.deactivate();
    accounts.saveAndFlush(manager);
    assertThatThrownBy(() -> reset.reset(new PasswordResetRequest(grant.token(), NEW_PASSWORD)))
        .isInstanceOf(InvalidRequestException.class);
    assertThat(
            passwords.matches(
                PASSWORD, accounts.findById(manager.getId()).orElseThrow().getPasswordHash()))
        .isTrue();
  }

  @Test
  void repeatedRequestsAreThrottledAndOldLinksAreReplaced() {
    var first = reset.issue(manager.getEmail()).orElseThrow();
    assertThat(reset.issue(manager.getEmail())).isEmpty();
    when(clock.instant()).thenReturn(NOW.plusSeconds(61));
    var second = reset.issue(manager.getEmail()).orElseThrow();
    assertThat(second.token()).isNotEqualTo(first.token());
    assertThatThrownBy(() -> reset.reset(new PasswordResetRequest(first.token(), NEW_PASSWORD)))
        .isInstanceOf(InvalidRequestException.class);
    reset.reset(new PasswordResetRequest(second.token(), NEW_PASSWORD));
    assertThat(tokens.count()).isZero();
  }

  @Test
  void recoveryRequestsReturnTheSameResponseForKnownAndUnknownAccountsAndSendPrivately()
      throws Exception {
    var delivered = new CompletableFuture<String>();
    doAnswer(
            invocation -> {
              delivered.complete(invocation.getArgument(1));
              return null;
            })
        .when(mail)
        .send(anyString(), anyString());
    try (var client = new SessionClient(port)) {
      var csrf = client.csrf();
      var existing =
          client.json(
              "POST",
              "/api/v1/admin/auth/password/forgot",
              Map.of("email", manager.getEmail()),
              csrf);
      var missing =
          client.json(
              "POST",
              "/api/v1/admin/auth/password/forgot",
              Map.of("email", "unknown@example.com"),
              csrf);
      assertThat(existing.statusCode()).isEqualTo(202);
      assertThat(missing.statusCode()).isEqualTo(202);
      assertThat(existing.body()).isEqualTo(missing.body()).isEmpty();
      var token = delivered.get(5, TimeUnit.SECONDS);
      assertThat(token).hasSize(43);
      assertThat(existing.body()).doesNotContain(token);
      verify(mail).send(manager.getEmail(), token);
    }
  }

  @Test
  void missingMailConfigurationReturnsUnavailableInsteadOfPretendingAnEmailWasSent()
      throws Exception {
    doThrow(new ServiceUnavailableException("Recuperação por e-mail indisponível."))
        .when(mail)
        .requireConfigured();
    try (var client = new SessionClient(port)) {
      for (var email : new String[] {manager.getEmail(), "unknown@example.com"}) {
        assertThat(
                client
                    .json(
                        "POST",
                        "/api/v1/admin/auth/password/forgot",
                        Map.of("email", email),
                        client.csrf())
                    .statusCode())
            .isEqualTo(503);
      }
      assertThat(tokens.count()).isZero();
    }
  }

  @Test
  void concurrentUseOfTheSameLinkChangesThePasswordOnlyOnce() throws Exception {
    var token = reset.issue(manager.getEmail()).orElseThrow().token();
    var start = new CountDownLatch(1);
    try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
      var first = executor.submit(() -> consumeAfter(start, token));
      var second = executor.submit(() -> consumeAfter(start, token));
      start.countDown();
      assertThat(java.util.List.of(first.get(), second.get()))
          .containsExactlyInAnyOrder(true, false);
      assertThat(events.count()).isEqualTo(1);
    }
  }

  @Test
  void operationalRecoveryRestoresAnExistingAdminAndDoesNotCreateAccounts() {
    manager.deactivate();
    manager.replacePermissions(java.util.Set.of());
    accounts.saveAndFlush(manager);
    operational.recover("MANAGER@EXAMPLE.COM", NEW_PASSWORD);
    var restored =
        accounts.findByEmailAndAccessRole(manager.getEmail(), AccessRole.ADMIN).orElseThrow();
    assertThat(restored.getId()).isEqualTo(manager.getId());
    assertThat(restored.isActive()).isTrue();
    assertThat(restored.getPermissions()).containsExactlyInAnyOrder(AdminPermission.values());
    assertThat(passwords.matches(NEW_PASSWORD, restored.getPasswordHash())).isTrue();
    assertThat(accounts.count()).isEqualTo(1);
    assertThatThrownBy(() -> operational.recover("missing@example.com", NEW_PASSWORD))
        .isInstanceOf(br.com.mutua.createchhub.shared.error.ResourceNotFoundException.class);
    assertThatThrownBy(() -> operational.recover(manager.getEmail(), "short"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void recoveryWritesRequireCsrfAndValidatePasswords() throws Exception {
    try (var client = new SessionClient(port)) {
      assertThat(
              client
                  .json(
                      "POST",
                      "/api/v1/admin/auth/password/forgot",
                      Map.of("email", manager.getEmail()),
                      null)
                  .statusCode())
          .isEqualTo(403);
      var token = reset.issue(manager.getEmail()).orElseThrow().token();
      assertThat(
              client
                  .json(
                      "POST",
                      "/api/v1/admin/auth/password/reset",
                      Map.of("token", token, "newPassword", "short"),
                      client.csrf())
                  .statusCode())
          .isEqualTo(400);
      assertThat(tokens.count()).isEqualTo(1);
    }
  }

  private boolean consumeAfter(CountDownLatch start, String token) throws InterruptedException {
    start.await();
    try {
      reset.reset(new PasswordResetRequest(token, NEW_PASSWORD));
      return true;
    } catch (InvalidRequestException exception) {
      return false;
    }
  }
}
