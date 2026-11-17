package br.com.mutua.createchhub.space;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.mutua.createchhub.support.PostgresTestConfiguration;
import br.com.mutua.createchhub.support.SessionClient;
import br.com.mutua.createchhub.user.AccessRole;
import br.com.mutua.createchhub.user.AdminPermission;
import br.com.mutua.createchhub.user.UserAccount;
import br.com.mutua.createchhub.user.UserAccountRepository;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(PostgresTestConfiguration.class)
class RoomArrangementApiTests {

  private static final String ROOT = "/api/v1/admin/spaces/";
  private static final String PASSWORD = "Room-layout-test-password";
  private final JsonMapper json = JsonMapper.builder().build();
  @LocalServerPort private int port;
  @Autowired private SpaceRepository spaces;
  @Autowired private UserAccountRepository accounts;
  @Autowired private PasswordEncoder passwords;
  private UUID room;

  @BeforeEach
  void prepare() {
    room = spaces.saveAndFlush(new Space("Coworking", SpaceType.ROOM, 16, null)).getId();
    var account =
        new UserAccount(
            "Operador", "arrangement@example.com", passwords.encode(PASSWORD), AccessRole.ADMIN);
    account.replacePermissions(Set.of(AdminPermission.MANAGE_SPACES));
    accounts.saveAndFlush(account);
  }

  @AfterEach
  void cleanup() {
    spaces.deleteAllInBatch();
    accounts.deleteAll();
  }

  @Test
  void generatesNumberedStationsAndAppendsWithoutReplacingExistingStations() throws Exception {
    try (var client = admin()) {
      assertThat(
              client
                  .json(
                      "PUT",
                      ROOT + room + "/booking-mode",
                      Map.of("bookingMode", "PER_STATION"),
                      client.csrf())
                  .statusCode())
          .isEqualTo(200);
      var response =
          client.json(
              "POST",
              ROOT + room + "/stations/batch",
              Map.of("count", 12, "columns", 4),
              client.csrf());
      assertThat(response.statusCode()).isEqualTo(201);
      var stations = json.readTree(response.body());
      assertThat(stations.size()).isEqualTo(12);
      assertThat(stations.path(0).path("stationPlacement").path("number").asInt()).isEqualTo(1);
      assertThat(stations.path(11).path("stationPlacement").path("number").asInt()).isEqualTo(12);
      assertThat(stations.path(0).path("stationPlacement").path("x").asDouble()).isEqualTo(12.5);
      assertThat(stations.path(0).path("stationPlacement").path("y").asDouble()).isEqualTo(16.67);
      var first = stations.path(0);
      assertThat(json.readTree(client.get(ROOT + first.path("id").asString()).body()))
          .isEqualTo(first);
      var more =
          client.json(
              "POST",
              ROOT + room + "/stations/batch",
              Map.of("count", 4, "columns", 4),
              client.csrf());
      assertThat(more.statusCode()).isEqualTo(201);
      assertThat(json.readTree(more.body()).path(0).path("stationPlacement").path("number").asInt())
          .isEqualTo(13);
      assertThat(json.readTree(client.get(ROOT + first.path("id").asString()).body()))
          .isEqualTo(first);
      assertThat(spaces.count()).isEqualTo(17);
    }
  }

  @Test
  void savesPositionsAtomicallyAndRejectsOutdatedOrDuplicateChanges() throws Exception {
    try (var client = admin()) {
      var stations = generate(client, 2);
      var first = stations.path(0);
      var second = stations.path(1);
      var update = position(first, 20, 30);
      var response =
          client.json(
              "PUT",
              ROOT + room + "/station-positions",
              Map.of("positions", List.of(update)),
              client.csrf());
      assertThat(response.statusCode()).isEqualTo(200);
      var saved = json.readTree(response.body()).path(0);
      assertThat(saved.path("version").asLong()).isEqualTo(first.path("version").asLong() + 1);
      assertThat(saved.path("stationPlacement").path("x").asDouble()).isEqualTo(20);
      assertThat(saved.path("id")).isEqualTo(first.path("id"));
      var staleBatch = Map.of("positions", List.of(position(second, 55, 60), update));
      assertThat(
              client
                  .json("PUT", ROOT + room + "/station-positions", staleBatch, client.csrf())
                  .statusCode())
          .isEqualTo(409);
      assertThat(json.readTree(client.get(ROOT + second.path("id").asString()).body()))
          .isEqualTo(second);
      assertThat(
              client
                  .json(
                      "PUT",
                      ROOT + room + "/station-positions",
                      Map.of(
                          "positions", List.of(position(saved, 25, 30), position(saved, 40, 50))),
                      client.csrf())
                  .statusCode())
          .isEqualTo(400);
    }
  }

  @Test
  void requiresStationModeAndPreservesExistingLinksWhenChangingMode() throws Exception {
    try (var client = admin()) {
      assertThat(
              client
                  .json(
                      "POST",
                      ROOT + room + "/stations/batch",
                      Map.of("count", 2, "columns", 2),
                      client.csrf())
                  .statusCode())
          .isEqualTo(409);
      var stations = generate(client, 1);
      assertThat(
              client
                  .json(
                      "PUT",
                      ROOT + room + "/booking-mode",
                      Map.of("bookingMode", "WHOLE_SPACE"),
                      client.csrf())
                  .statusCode())
          .isEqualTo(409);
      assertThat(
              client
                  .json(
                      "PUT",
                      ROOT + stations.path(0).path("id").asString() + "/booking-mode",
                      Map.of("bookingMode", "PER_STATION"),
                      client.csrf())
                  .statusCode())
          .isEqualTo(409);
      assertThat(
              client
                  .json(
                      "PUT",
                      ROOT + room + "/booking-mode",
                      Map.of("bookingMode", "BAD"),
                      client.csrf())
                  .statusCode())
          .isEqualTo(400);
    }
  }

  @ParameterizedTest
  @CsvSource({"0,4", "101,4", "4,0", "4,101"})
  void rejectsInvalidBatchSizes(int count, int columns) throws Exception {
    try (var client = admin()) {
      assertThat(
              client
                  .json(
                      "POST",
                      ROOT + room + "/stations/batch",
                      Map.of("count", count, "columns", columns),
                      client.csrf())
                  .statusCode())
          .isEqualTo(400);
      assertThat(spaces.count()).isEqualTo(1);
    }
  }

  @Test
  void protectsTheNewWritesWithSessionPermissionAndCsrf() throws Exception {
    try (var visitor = new SessionClient(port);
        var client = admin()) {
      assertThat(
              visitor
                  .json(
                      "POST",
                      ROOT + room + "/stations/batch",
                      Map.of("count", 2, "columns", 2),
                      visitor.csrf())
                  .statusCode())
          .isEqualTo(401);
      assertThat(
              client
                  .json(
                      "POST",
                      ROOT + room + "/stations/batch",
                      Map.of("count", 2, "columns", 2),
                      null)
                  .statusCode())
          .isEqualTo(403);
      assertThat(
              client
                  .json(
                      "PUT",
                      ROOT + room + "/booking-mode",
                      Map.of("bookingMode", "PER_STATION"),
                      null)
                  .statusCode())
          .isEqualTo(403);
    }
    var account =
        new UserAccount(
            "Gestor", "access@example.com", passwords.encode(PASSWORD), AccessRole.ADMIN);
    account.replacePermissions(Set.of(AdminPermission.MANAGE_ACCESS));
    accounts.saveAndFlush(account);
    try (var client = new SessionClient(port)) {
      client.post(
          "/api/v1/admin/auth/login",
          Map.of("email", "access@example.com", "password", PASSWORD),
          client.csrf());
      assertThat(
              client
                  .json(
                      "PUT",
                      ROOT + room + "/booking-mode",
                      Map.of("bookingMode", "PER_STATION"),
                      client.csrf())
                  .statusCode())
          .isEqualTo(403);
      assertThat(
              client
                  .json(
                      "POST",
                      ROOT + room + "/stations/batch",
                      Map.of("count", 2, "columns", 2),
                      client.csrf())
                  .statusCode())
          .isEqualTo(403);
      assertThat(
              client
                  .json(
                      "PUT",
                      ROOT + room + "/station-positions",
                      Map.of(
                          "positions",
                          List.of(Map.of("id", UUID.randomUUID(), "version", 0, "x", 10, "y", 20))),
                      client.csrf())
                  .statusCode())
          .isEqualTo(403);
    }
  }

  private JsonNode generate(SessionClient client, int count) throws Exception {
    client.json(
        "PUT", ROOT + room + "/booking-mode", Map.of("bookingMode", "PER_STATION"), client.csrf());
    var response =
        client.json(
            "POST",
            ROOT + room + "/stations/batch",
            Map.of("count", count, "columns", 2),
            client.csrf());
    assertThat(response.statusCode()).isEqualTo(201);
    return json.readTree(response.body());
  }

  private Map<String, Object> position(JsonNode station, int x, int y) {
    return Map.of(
        "id",
        station.path("id").asString(),
        "version",
        station.path("version").asLong(),
        "x",
        x,
        "y",
        y);
  }

  private SessionClient admin() throws Exception {
    var client = new SessionClient(port);
    assertThat(
            client
                .post(
                    "/api/v1/admin/auth/login",
                    Map.of("email", "arrangement@example.com", "password", PASSWORD),
                    client.csrf())
                .statusCode())
        .isEqualTo(204);
    return client;
  }
}
