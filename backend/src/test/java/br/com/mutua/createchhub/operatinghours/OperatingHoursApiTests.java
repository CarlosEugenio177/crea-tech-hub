package br.com.mutua.createchhub.operatinghours;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.mutua.createchhub.support.PostgresTestConfiguration;
import br.com.mutua.createchhub.support.SessionClient;
import br.com.mutua.createchhub.user.AccessRole;
import br.com.mutua.createchhub.user.AdminPermission;
import br.com.mutua.createchhub.user.UserAccount;
import br.com.mutua.createchhub.user.UserAccountRepository;
import java.time.DayOfWeek;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(PostgresTestConfiguration.class)
class OperatingHoursApiTests {

  private static final String ADMIN_PATH = "/api/v1/admin/hub/operating-hours";
  private static final String PUBLIC_PATH = "/api/v1/hub/operating-hours";
  private static final String USAGE_RULES_PATH = "/api/v1/hub/usage-rules";
  private static final String PASSWORD = "Operating-hours-test-password";
  private static final Set<DayOfWeek> WEEKDAYS =
      Set.of(
          DayOfWeek.MONDAY,
          DayOfWeek.TUESDAY,
          DayOfWeek.WEDNESDAY,
          DayOfWeek.THURSDAY,
          DayOfWeek.FRIDAY);
  private final JsonMapper json = JsonMapper.builder().build();

  @LocalServerPort private int port;
  @Autowired private OperatingHoursService hours;
  @Autowired private UserAccountRepository accounts;
  @Autowired private PasswordEncoder passwords;

  @BeforeEach
  void prepare() {
    restoreDefaults();
    var account =
        new UserAccount(
            "Responsável", "hours@example.com", passwords.encode(PASSWORD), AccessRole.ADMIN);
    account.replacePermissions(Set.of(AdminPermission.MANAGE_SPACES));
    accounts.saveAndFlush(account);
  }

  @AfterEach
  void cleanup() {
    restoreDefaults();
    accounts.deleteAll();
  }

  @Test
  void publishesTheDefaultScheduleWithoutLoginAndProtectsAdministration() throws Exception {
    try (var client = new SessionClient(port)) {
      var response = client.get(PUBLIC_PATH);
      assertThat(response.statusCode()).isEqualTo(200);
      var body = json.readTree(response.body());
      assertThat(body.path("openingTime").asString()).isEqualTo("08:00");
      assertThat(body.path("closingTime").asString()).isEqualTo("18:00");
      assertThat(body.path("daysOfWeek"))
          .isEqualTo(
              json.readTree("[\"MONDAY\",\"TUESDAY\",\"WEDNESDAY\",\"THURSDAY\",\"FRIDAY\"]"));
      assertThat(client.get(ADMIN_PATH).statusCode()).isEqualTo(401);
      assertThat(client.json("PUT", ADMIN_PATH, validRequest(), client.csrf()).statusCode())
          .isEqualTo(401);
    }
  }

  @Test
  void publishesUsageRulesWithoutLogin() throws Exception {
    try (var client = new SessionClient(port)) {
      var response = client.get(USAGE_RULES_PATH);

      assertThat(response.statusCode()).isEqualTo(200);
      assertThat(response.headers().firstValue("Content-Type").orElseThrow())
          .contains("application/json");
      var body = json.readTree(response.body());
      assertThat(body.path("acceptanceRequired").asBoolean()).isTrue();
      assertThat(body.path("generalRules").isArray()).isTrue();
      assertThat(body.path("generalRules").size()).isEqualTo(4);
      assertThat(body.path("generalRules").get(0).asString())
          .isEqualTo("Todas as solicitações passam por análise manual antes da confirmação.");
    }
  }

  @Test
  void updatesTheWholeConfigurationAndPublishesThePersistedValues() throws Exception {
    try (var client = admin()) {
      var response = client.json("PUT", ADMIN_PATH, validRequest(), client.csrf());
      assertThat(response.statusCode()).isEqualTo(200);
      var body = json.readTree(response.body());
      assertThat(body.path("daysOfWeek")).isEqualTo(json.readTree("[\"SATURDAY\",\"SUNDAY\"]"));
      assertThat(body.path("openingTime").asString()).isEqualTo("09:15");
      assertThat(body.path("closingTime").asString()).isEqualTo("17:45");
      assertThat(json.readTree(client.get(ADMIN_PATH).body())).isEqualTo(body);
      try (var visitor = new SessionClient(port)) {
        assertThat(json.readTree(visitor.get(PUBLIC_PATH).body())).isEqualTo(body);
      }
    }
  }

  @ParameterizedTest
  @MethodSource("invalidFields")
  void rejectsInvalidSettingsWithoutChangingTheConfiguration(String field, Object value)
      throws Exception {
    var request = new HashMap<>(validRequest());
    request.put(field, value);
    var previous = hours.current();
    try (var client = admin()) {
      var response = client.json("PUT", ADMIN_PATH, request, client.csrf());
      assertThat(response.statusCode()).isEqualTo(400);
      assertThat(response.headers().firstValue("Content-Type").orElseThrow())
          .contains("application/problem+json");
      assertThat(hours.current()).isEqualTo(previous);
    }
  }

  static Stream<Arguments> invalidFields() {
    return Stream.of(
        Arguments.of("daysOfWeek", List.of()),
        Arguments.of("daysOfWeek", null),
        Arguments.of("daysOfWeek", List.of("FUNDAY")),
        Arguments.of("daysOfWeek", List.of(1)),
        Arguments.of("openingTime", "9:15"),
        Arguments.of("openingTime", "09:15:30"),
        Arguments.of("openingTime", "24:00"),
        Arguments.of("openingTime", null),
        Arguments.of("closingTime", "09:15"),
        Arguments.of("closingTime", "08:00"),
        Arguments.of("closingTime", "25:00"),
        Arguments.of("closingTime", "18:60"));
  }

  @Test
  void requiresCsrfAndDoesNotAllowAccessManagementToBypassSpacePermissions() throws Exception {
    try (var client = admin()) {
      assertThat(client.json("PUT", ADMIN_PATH, validRequest(), null).statusCode()).isEqualTo(403);
    }
    var manager =
        new UserAccount(
            "Gestor", "access@example.com", passwords.encode(PASSWORD), AccessRole.ADMIN);
    manager.replacePermissions(Set.of(AdminPermission.MANAGE_ACCESS));
    accounts.saveAndFlush(manager);
    try (var client = new SessionClient(port)) {
      assertThat(
              client
                  .post(
                      "/api/v1/admin/auth/login",
                      Map.of("email", "access@example.com", "password", PASSWORD),
                      client.csrf())
                  .statusCode())
          .isEqualTo(204);
      assertThat(client.get(ADMIN_PATH).statusCode()).isEqualTo(403);
      assertThat(client.json("PUT", ADMIN_PATH, validRequest(), client.csrf()).statusCode())
          .isEqualTo(403);
    }
    assertThat(hours.current().openingTime()).isEqualTo("08:00");
  }

  @Test
  void concurrentUpdatesKeepDaysAndTimesFromTheSameConfiguration() throws Exception {
    var first = new OperatingHoursRequest(Set.of(DayOfWeek.MONDAY), "06:15", "12:30");
    var second = new OperatingHoursRequest(Set.of(DayOfWeek.SUNDAY), "10:00", "23:59");
    var start = new CountDownLatch(1);
    try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
      var left =
          executor.submit(
              () -> {
                start.await();
                return hours.update(first);
              });
      var right =
          executor.submit(
              () -> {
                start.await();
                return hours.update(second);
              });
      start.countDown();
      var leftResult = left.get(10, TimeUnit.SECONDS);
      var rightResult = right.get(10, TimeUnit.SECONDS);
      assertThat(leftResult.openingTime()).isEqualTo("06:15");
      assertThat(leftResult.daysOfWeek()).containsExactly(DayOfWeek.MONDAY);
      assertThat(rightResult.openingTime()).isEqualTo("10:00");
      assertThat(rightResult.daysOfWeek()).containsExactly(DayOfWeek.SUNDAY);
      assertThat(hours.current()).isIn(leftResult, rightResult);
    }
  }

  private Map<String, Object> validRequest() {
    return Map.of(
        "daysOfWeek",
        List.of("SUNDAY", "SATURDAY"),
        "openingTime",
        "09:15",
        "closingTime",
        "17:45");
  }

  private SessionClient admin() throws Exception {
    var client = new SessionClient(port);
    assertThat(
            client
                .post(
                    "/api/v1/admin/auth/login",
                    Map.of("email", "hours@example.com", "password", PASSWORD),
                    client.csrf())
                .statusCode())
        .isEqualTo(204);
    return client;
  }

  private void restoreDefaults() {
    hours.update(new OperatingHoursRequest(WEEKDAYS, "08:00", "18:00"));
  }
}
