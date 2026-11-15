package br.com.mutua.createchhub.access;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.mutua.createchhub.support.PostgresTestConfiguration;
import br.com.mutua.createchhub.support.SessionClient;
import br.com.mutua.createchhub.user.AccessRole;
import br.com.mutua.createchhub.user.AdminPermission;
import br.com.mutua.createchhub.user.UserAccount;
import br.com.mutua.createchhub.user.UserAccountRepository;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(PostgresTestConfiguration.class)
class AdminTeamTests {

  private static final String PATH = "/api/v1/admin/team";
  private static final String PASSWORD = "Initial-test-password";
  private static final String NEW_PASSWORD = "New-personal-test-password";
  private final JsonMapper json = JsonMapper.builder().build();

  @LocalServerPort private int port;
  @Autowired private UserAccountRepository accounts;
  @Autowired private PasswordEncoder passwords;
  @Autowired private AccessAuditRepository events;
  @Autowired private AdminTeamService team;
  private UserAccount manager;

  @BeforeEach
  void prepare() {
    manager = account("manager@example.com", EnumSet.allOf(AdminPermission.class));
  }

  @AfterEach
  void cleanup() {
    events.deleteAll();
    accounts.deleteAll();
  }

  @Test
  void createsStaffForcesPasswordChangeAndPromotesWithoutChangingTheRole() throws Exception {
    try (var owner = login("manager@example.com", PASSWORD)) {
      var response =
          owner.json(
              "POST",
              PATH,
              Map.of(
                  "fullName",
                  "Funcionário",
                  "email",
                  "STAFF@EXAMPLE.COM",
                  "initialPassword",
                  PASSWORD,
                  "permissions",
                  List.of("MANAGE_SPACES")),
              owner.csrf());
      assertThat(response.statusCode()).isEqualTo(201);
      assertThat(response.body()).doesNotContain(PASSWORD, "passwordHash", "initialPassword");
      var created = json.readTree(response.body());
      assertThat(created.path("role").asString()).isEqualTo("ADMIN");
      assertThat(created.path("passwordChangeRequired").asBoolean()).isTrue();
      var id = created.path("id").asString();
      assertThat(owner.get(PATH + "/" + id).statusCode()).isEqualTo(200);
      try (var staff = login("staff@example.com", PASSWORD)) {
        assertThat(staff.get("/api/v1/admin/spaces").statusCode()).isEqualTo(403);
        assertThat(
                staff
                    .json(
                        "POST",
                        "/api/v1/admin/auth/password",
                        Map.of("currentPassword", PASSWORD, "newPassword", NEW_PASSWORD),
                        staff.csrf())
                    .statusCode())
            .isEqualTo(204);
        assertThat(staff.get("/api/v1/admin/auth/me").statusCode()).isEqualTo(401);
      }
      try (var staff = login("staff@example.com", NEW_PASSWORD)) {
        assertThat(staff.get("/api/v1/admin/spaces").statusCode()).isEqualTo(200);
        assertThat(staff.get(PATH).statusCode()).isEqualTo(403);
        var promote =
            owner.json(
                "PUT",
                PATH + "/" + id + "/permissions",
                Map.of("permissions", List.of("MANAGE_ACCESS", "MANAGE_SPACES")),
                owner.csrf());
        assertThat(promote.statusCode()).isEqualTo(200);
        assertThat(json.readTree(promote.body()).path("role").asString()).isEqualTo("ADMIN");
        assertThat(staff.get(PATH).statusCode()).isEqualTo(401);
      }
      try (var promoted = login("staff@example.com", NEW_PASSWORD)) {
        assertThat(promoted.get(PATH).statusCode()).isEqualTo(200);
        assertThat(
                owner
                    .json(
                        "PUT",
                        PATH + "/" + id + "/permissions",
                        Map.of("permissions", List.of("MANAGE_SPACES")),
                        owner.csrf())
                    .statusCode())
            .isEqualTo(200);
        assertThat(promoted.get(PATH).statusCode()).isEqualTo(401);
      }
      try (var demoted = login("staff@example.com", NEW_PASSWORD)) {
        assertThat(demoted.get(PATH).statusCode()).isEqualTo(403);
        assertThat(demoted.get("/api/v1/admin/spaces").statusCode()).isEqualTo(200);
      }
      var audit = owner.get(PATH + "/audit");
      assertThat(audit.statusCode()).isEqualTo(200);
      assertThat(audit.body())
          .contains("ADMIN_CREATED", "PERMISSIONS_CHANGED", "PASSWORD_CHANGED")
          .doesNotContain(PASSWORD, NEW_PASSWORD, "passwordHash");
    }
  }

  @Test
  void disablingStaffRevokesExistingSessionsAndReactivationPreservesIdentity() throws Exception {
    var account = account("staff@example.com", Set.of(AdminPermission.MANAGE_SPACES));
    try (var owner = login("manager@example.com", PASSWORD);
        var staff = login("staff@example.com", PASSWORD)) {
      var path = PATH + "/" + account.getId() + "/status";
      assertThat(owner.json("PATCH", path, Map.of("active", false), owner.csrf()).statusCode())
          .isEqualTo(200);
      assertThat(staff.get("/api/v1/admin/spaces").statusCode()).isEqualTo(401);
      assertThat(accounts.existsById(account.getId())).isTrue();
      assertThat(owner.json("PATCH", path, Map.of("active", true), owner.csrf()).statusCode())
          .isEqualTo(200);
    }
    try (var staff = login("staff@example.com", PASSWORD)) {
      assertThat(staff.get("/api/v1/admin/spaces").statusCode()).isEqualTo(200);
    }
  }

  @Test
  void manageAccessDoesNotImplicitlyGrantSpaceManagement() throws Exception {
    account("access@example.com", Set.of(AdminPermission.MANAGE_ACCESS));
    try (var client = login("access@example.com", PASSWORD)) {
      assertThat(client.get(PATH + "/permissions").statusCode()).isEqualTo(200);
      assertThat(client.get("/api/v1/admin/spaces").statusCode()).isEqualTo(403);
    }
  }

  @Test
  void refusesSelfPromotionAndSelfDeactivation() throws Exception {
    try (var owner = login("manager@example.com", PASSWORD)) {
      var path = PATH + "/" + manager.getId();
      assertThat(
              owner
                  .json(
                      "PUT", path + "/permissions", Map.of("permissions", List.of()), owner.csrf())
                  .statusCode())
          .isEqualTo(409);
      assertThat(
              owner
                  .json("PATCH", path + "/status", Map.of("active", false), owner.csrf())
                  .statusCode())
          .isEqualTo(409);
      assertThat(
              accounts.countActiveWithPermission(AccessRole.ADMIN, AdminPermission.MANAGE_ACCESS))
          .isEqualTo(1);
      assertThat(events.count()).isZero();
    }
  }

  @ParameterizedTest
  @ValueSource(strings = {"fullName", "email", "initialPassword", "permissions"})
  void validatesAdministrativeRegistration(String invalidField) throws Exception {
    var request =
        new java.util.HashMap<String, Object>(
            Map.of(
                "fullName",
                "Nome",
                "email",
                "new@example.com",
                "initialPassword",
                PASSWORD,
                "permissions",
                List.of("MANAGE_SPACES")));
    request.put(invalidField, invalidField.equals("permissions") ? List.of("SUPER_ADMIN") : " ");
    try (var owner = login("manager@example.com", PASSWORD)) {
      assertThat(owner.json("POST", PATH, request, owner.csrf()).statusCode()).isEqualTo(400);
      assertThat(accounts.count()).isEqualTo(1);
    }
  }

  @Test
  void rejectsDuplicateEmailsAndDoesNotManageRequesterAccounts() throws Exception {
    var requester =
        accounts.saveAndFlush(
            new UserAccount(
                "Visitante", "requester@example.com", passwords.encode(PASSWORD), AccessRole.USER));
    try (var owner = login("manager@example.com", PASSWORD)) {
      assertThat(
              owner
                  .json(
                      "POST",
                      PATH,
                      Map.of(
                          "fullName",
                          "Duplicado",
                          "email",
                          "MANAGER@EXAMPLE.COM",
                          "initialPassword",
                          PASSWORD,
                          "permissions",
                          List.of()),
                      owner.csrf())
                  .statusCode())
          .isEqualTo(409);
      assertThat(
              owner
                  .json(
                      "PUT",
                      PATH + "/" + requester.getId() + "/permissions",
                      Map.of("permissions", List.of("MANAGE_ACCESS")),
                      owner.csrf())
                  .statusCode())
          .isEqualTo(404);
      assertThat(owner.get(PATH + "?size=1").body()).contains("\"totalElements\":1");
    }
  }

  @Test
  void refusesAdministrativeWritesWithoutCsrf() throws Exception {
    try (var owner = login("manager@example.com", PASSWORD)) {
      assertThat(
              owner
                  .json(
                      "PATCH",
                      PATH + "/" + manager.getId() + "/status",
                      Map.of("active", false),
                      null)
                  .statusCode())
          .isEqualTo(403);
    }
  }

  @Test
  void concurrentRevocationsLeaveOneManagerAndRejectTheRevokedActor() throws Exception {
    var second = account("second@example.com", Set.of(AdminPermission.MANAGE_ACCESS));
    var start = new CountDownLatch(1);
    try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
      var first = executor.submit(() -> revokeAfter(start, manager.getId(), second.getId()));
      var other = executor.submit(() -> revokeAfter(start, second.getId(), manager.getId()));
      start.countDown();
      assertThat(List.of(first.get(), other.get())).containsExactlyInAnyOrder(true, false);
      assertThat(
              accounts.countActiveWithPermission(AccessRole.ADMIN, AdminPermission.MANAGE_ACCESS))
          .isEqualTo(1);
    }
  }

  private boolean revokeAfter(CountDownLatch start, UUID actor, UUID target)
      throws InterruptedException {
    start.await();
    try {
      team.status(actor, target, false);
      return true;
    } catch (AccessDeniedException exception) {
      return false;
    }
  }

  private UserAccount account(String email, Set<AdminPermission> permissions) {
    var account = new UserAccount("Equipe", email, passwords.encode(PASSWORD), AccessRole.ADMIN);
    account.replacePermissions(permissions);
    return accounts.saveAndFlush(account);
  }

  private SessionClient login(String email, String password) throws Exception {
    var client = new SessionClient(port);
    assertThat(
            client
                .post(
                    "/api/v1/admin/auth/login",
                    Map.of("email", email, "password", password),
                    client.csrf())
                .statusCode())
        .isEqualTo(204);
    return client;
  }
}
