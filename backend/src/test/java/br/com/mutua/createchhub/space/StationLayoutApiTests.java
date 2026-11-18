package br.com.mutua.createchhub.space;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.mutua.createchhub.block.SpaceBlockRepository;
import br.com.mutua.createchhub.support.PostgresTestConfiguration;
import br.com.mutua.createchhub.support.SessionClient;
import br.com.mutua.createchhub.user.AccessRole;
import br.com.mutua.createchhub.user.AdminPermission;
import br.com.mutua.createchhub.user.UserAccount;
import br.com.mutua.createchhub.user.UserAccountRepository;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(PostgresTestConfiguration.class)
class StationLayoutApiTests {

  private static final String PATH = "/api/v1/admin/spaces/";
  private static final String PASSWORD = "Station-layout-test-password";
  private final JsonMapper json = JsonMapper.builder().build();

  @LocalServerPort private int port;
  @Autowired private SpaceRepository spaces;
  @Autowired private SpaceBlockRepository blocks;
  @Autowired private StationLayoutService layout;
  @Autowired private UserAccountRepository accounts;
  @Autowired private PasswordEncoder passwords;
  private Space room;
  private Space station;

  @BeforeEach
  void prepare() {
    room = spaces.saveAndFlush(new Space("Coworking", SpaceType.ROOM, 16, null));
    room.changeBookingMode(BookingMode.PER_STATION);
    room = spaces.saveAndFlush(room);
    station = spaces.saveAndFlush(new Space("Estação", SpaceType.WORKSTATION, 1, null));
    var account =
        new UserAccount(
            "Operador", "layout@example.com", passwords.encode(PASSWORD), AccessRole.ADMIN);
    account.replacePermissions(Set.of(AdminPermission.MANAGE_SPACES));
    accounts.saveAndFlush(account);
  }

  @AfterEach
  void cleanup() {
    blocks.deleteAllInBatch();
    spaces.deleteAllInBatch();
    accounts.deleteAll();
  }

  @Test
  void publicAvailabilityAppliesStationAndRoomBlocksWithoutExposingTheirReasons() throws Exception {
    var seats = layout.generate(room.getId(), new StationBatchRequest(2, 2));
    String base = "/api/v1/spaces/" + room.getId() + "/availability";
    String interval = "?startsAt=2099-03-10T09:00&endsAt=2099-03-10T10:00";
    String endpoint = "/api/v1/admin/space-blocks";
    try (var client = admin();
        var visitor = new SessionClient(port)) {
      var csrf = client.csrf();
      var stationBlock =
          Map.of(
              "spaceId",
              seats.getFirst().id(),
              "startsAt",
              "2099-03-10T09:30",
              "endsAt",
              "2099-03-10T10:00",
              "reason",
              "Motivo interno");
      assertThat(client.json("POST", endpoint, stationBlock, csrf).statusCode()).isEqualTo(201);
      var result = visitor.get(base + interval);
      assertThat(result.statusCode()).isEqualTo(200);
      assertThat(result.body()).doesNotContain("Motivo interno", "createdBy");
      var body = json.readTree(result.body());
      assertThat(body.path("status").asString()).isEqualTo("AVAILABLE");
      assertThat(body.path("stations").get(0).path("available").asBoolean()).isFalse();
      assertThat(body.path("stations").get(1).path("available").asBoolean()).isTrue();
      var adjacent = visitor.get(base + "?startsAt=2099-03-10T10:00&endsAt=2099-03-10T11:00");
      assertThat(
              json.readTree(adjacent.body()).path("stations").get(0).path("available").asBoolean())
          .isTrue();
      var roomBlock = new HashMap<>(stationBlock);
      roomBlock.put("spaceId", room.getId());
      var created = client.json("POST", endpoint, roomBlock, csrf);
      assertThat(created.statusCode()).isEqualTo(201);
      var blocked = json.readTree(visitor.get(base + interval).body());
      assertThat(blocked.path("status").asString()).isEqualTo("UNAVAILABLE");
      assertThat(blocked.path("stations").get(1).path("available").asBoolean()).isFalse();
      var roomBlockId = json.readTree(created.body()).path("id").asString();
      assertThat(
              client
                  .json("POST", endpoint + "/" + roomBlockId + "/cancel", null, csrf)
                  .statusCode())
          .isEqualTo(200);
      assertThat(json.readTree(visitor.get(base + interval).body()).path("status").asString())
          .isEqualTo("AVAILABLE");
      var closed = visitor.get(base + "?startsAt=2099-03-10T07:00&endsAt=2099-03-10T08:30");
      assertThat(json.readTree(closed.body()).path("status").asString()).isEqualTo("CLOSED");
      assertThat(
              visitor.get(base + "?startsAt=2099-02-31T09:00&endsAt=2099-02-31T10:00").statusCode())
          .isEqualTo(400);
    }
  }

  @Test
  void positionsRenumbersAndMovesAnExistingStationWithoutChangingItsIdentity() throws Exception {
    try (var client = admin()) {
      var csrf = client.csrf();
      var path = PATH + station.getId();
      assertThat(json.readTree(client.get(path).body()).path("stationPlacement").isMissingNode())
          .isTrue();
      var response = client.json("PUT", path + "/station-placement", request(), csrf);
      assertThat(response.statusCode()).isEqualTo(200);
      var body = json.readTree(response.body());
      assertThat(body.path("id").asString()).isEqualTo(station.getId().toString());
      assertThat(body.path("capacity").asInt()).isEqualTo(1);
      assertThat(body.path("stationPlacement").path("number").asInt()).isEqualTo(2);
      assertThat(body.path("stationPlacement").path("x").asDouble()).isEqualTo(25.5);
      var persistedPlacement = json.readTree(client.get(path).body()).path("stationPlacement");
      assertThat(persistedPlacement.path("roomId").asString()).isEqualTo(room.getId().toString());
      assertThat(persistedPlacement.path("number").asInt()).isEqualTo(2);
      assertThat(persistedPlacement.path("x").asDouble()).isEqualTo(25.5);
      assertThat(persistedPlacement.path("y").asDouble()).isEqualTo(40);

      var updated =
          client.json(
              "PUT",
              path,
              Map.of("name", "Estação revisada", "type", "WORKSTATION", "capacity", 1),
              csrf);
      assertThat(updated.statusCode()).isEqualTo(200);
      assertThat(json.readTree(updated.body()).path("stationPlacement"))
          .isEqualTo(persistedPlacement);
      var otherRoom = spaces.saveAndFlush(new Space("Estudos", SpaceType.ROOM, 11, null));
      otherRoom.changeBookingMode(BookingMode.PER_STATION);
      spaces.saveAndFlush(otherRoom);
      var moved = new HashMap<>(request());
      moved.put("roomId", otherRoom.getId());
      moved.put("number", 1);
      moved.put("x", 100);
      moved.put("y", 0);
      assertThat(client.json("PUT", path + "/station-placement", moved, csrf).statusCode())
          .isEqualTo(200);
      assertThat(
              json.readTree(client.get(PATH + room.getId() + "/stations").body())
                  .path("totalElements")
                  .asInt())
          .isZero();
      var current = json.readTree(client.get(path).body());
      assertThat(current.path("stationPlacement").path("roomId").asString())
          .isEqualTo(otherRoom.getId().toString());
      assertThat(current.path("createdAt")).isEqualTo(body.path("createdAt"));
    }
  }

  @Test
  void publishesOnlyActiveStationsOfActiveRoomsAndPaginatesByNumber() throws Exception {
    layout.place(station.getId(), placement(10));
    var first = spaces.saveAndFlush(new Space("Primeira", SpaceType.WORKSTATION, 1, null));
    layout.place(first.getId(), placement(2));
    try (var visitor = new SessionClient(port);
        var client = admin()) {
      var publicPath = "/api/v1/spaces/" + room.getId() + "/stations";
      var response = visitor.get(publicPath + "?size=1");
      assertThat(response.statusCode()).isEqualTo(200);
      var body = json.readTree(response.body());
      assertThat(body.path("totalElements").asInt()).isEqualTo(2);
      assertThat(body.path("items").path(0).path("stationPlacement").path("number").asInt())
          .isEqualTo(2);
      assertThat(
              json.readTree(visitor.get(publicPath + "?size=1&page=1").body())
                  .path("items")
                  .path(0)
                  .path("id")
                  .asString())
          .isEqualTo(station.getId().toString());
      assertThat(
              client
                  .json(
                      "PATCH",
                      PATH + station.getId() + "/status",
                      Map.of("active", false),
                      client.csrf())
                  .statusCode())
          .isEqualTo(200);
      assertThat(json.readTree(visitor.get(publicPath).body()).path("totalElements").asInt())
          .isEqualTo(1);
      assertThat(
              json.readTree(client.get(PATH + room.getId() + "/stations").body())
                  .path("totalElements")
                  .asInt())
          .isEqualTo(2);
      var catalogBefore = visitor.get("/api/v1/spaces");
      assertThat(catalogBefore.statusCode()).isEqualTo(200);
      assertThat(catalogBefore.body()).contains(first.getId().toString());
      assertThat(
              client
                  .json(
                      "PATCH",
                      PATH + room.getId() + "/status",
                      Map.of("active", false),
                      client.csrf())
                  .statusCode())
          .isEqualTo(200);
      assertThat(visitor.get(publicPath).statusCode()).isEqualTo(404);
      assertThat(client.get(PATH + room.getId() + "/stations").statusCode()).isEqualTo(200);
      var catalogAfter = visitor.get("/api/v1/spaces");
      assertThat(catalogAfter.statusCode()).isEqualTo(200);
      assertThat(catalogAfter.body()).doesNotContain(first.getId().toString());
    }
  }

  @Test
  void preventsDuplicateNumbersAndIncompatibleTypeChanges() throws Exception {
    layout.place(station.getId(), placement(2));
    var other = spaces.saveAndFlush(new Space("Outra", SpaceType.WORKSTATION, 1, null));
    try (var client = admin()) {
      var csrf = client.csrf();
      assertThat(
              client
                  .json("PUT", PATH + other.getId() + "/station-placement", request(), csrf)
                  .statusCode())
          .isEqualTo(409);
      assertThat(
              client
                  .json("PUT", PATH + station.getId() + "/station-placement", request(), csrf)
                  .statusCode())
          .isEqualTo(200);
      assertThat(
              client
                  .json(
                      "PUT",
                      PATH + room.getId(),
                      Map.of("name", "Mudou", "type", "SHARED_TABLE", "capacity", 16),
                      csrf)
                  .statusCode())
          .isEqualTo(409);
      assertThat(
              client
                  .json(
                      "PUT",
                      PATH + station.getId(),
                      Map.of("name", "Mudou", "type", "ROOM", "capacity", 1),
                      csrf)
                  .statusCode())
          .isEqualTo(409);
      assertThat(
              client
                  .json("PUT", PATH + room.getId() + "/station-placement", request(), csrf)
                  .statusCode())
          .isEqualTo(409);
      var wrongRoom = new HashMap<>(request());
      wrongRoom.put("roomId", other.getId());
      assertThat(
              client
                  .json("PUT", PATH + station.getId() + "/station-placement", wrongRoom, csrf)
                  .statusCode())
          .isEqualTo(409);
      assertThat(spaces.findById(station.getId()).orElseThrow().getRoomId())
          .isEqualTo(room.getId());
      assertThat(spaces.findById(room.getId()).orElseThrow().getName()).isEqualTo("Coworking");
    }
  }

  @ParameterizedTest
  @MethodSource("invalidFields")
  void rejectsInvalidPlacementWithoutChangingTheStation(String field, Object value)
      throws Exception {
    var request = new HashMap<>(request());
    request.put(field, value);
    try (var client = admin()) {
      var response =
          client.json("PUT", PATH + station.getId() + "/station-placement", request, client.csrf());
      assertThat(response.statusCode()).isEqualTo(400);
      assertThat(response.headers().firstValue("Content-Type").orElseThrow())
          .contains("application/problem+json");
      assertThat(spaces.findById(station.getId()).orElseThrow().getRoomId()).isNull();
    }
  }

  static Stream<Arguments> invalidFields() {
    return Stream.of(
        Arguments.of("roomId", null),
        Arguments.of("roomId", "invalid"),
        Arguments.of("number", null),
        Arguments.of("number", 0),
        Arguments.of("number", -1),
        Arguments.of("number", 1.5),
        Arguments.of("x", null),
        Arguments.of("x", -0.01),
        Arguments.of("x", 100.01),
        Arguments.of("x", 12.345),
        Arguments.of("y", null),
        Arguments.of("y", -1),
        Arguments.of("y", 101),
        Arguments.of("y", 12.345));
  }

  @ParameterizedTest
  @ValueSource(strings = {"page=-1", "size=0", "size=101", "page=abc"})
  void rejectsInvalidPublicPagination(String query) throws Exception {
    try (var visitor = new SessionClient(port)) {
      assertThat(visitor.get("/api/v1/spaces/" + room.getId() + "/stations?" + query).statusCode())
          .isEqualTo(400);
    }
  }

  @Test
  void returnsNotFoundForUnknownRoomsOrStations() throws Exception {
    try (var client = admin();
        var visitor = new SessionClient(port)) {
      assertThat(client.get(PATH + UUID.randomUUID() + "/stations").statusCode()).isEqualTo(404);
      assertThat(visitor.get("/api/v1/spaces/" + station.getId() + "/stations").statusCode())
          .isEqualTo(404);
      assertThat(
              client
                  .json(
                      "PUT",
                      PATH + UUID.randomUUID() + "/station-placement",
                      request(),
                      client.csrf())
                  .statusCode())
          .isEqualTo(404);
      var missingRoom = new HashMap<>(request());
      missingRoom.put("roomId", UUID.randomUUID());
      assertThat(
              client
                  .json(
                      "PUT",
                      PATH + station.getId() + "/station-placement",
                      missingRoom,
                      client.csrf())
                  .statusCode())
          .isEqualTo(404);
    }
  }

  @Test
  void requiresSpacePermissionsAuthenticationAndCsrf() throws Exception {
    var path = PATH + station.getId() + "/station-placement";
    try (var visitor = new SessionClient(port);
        var client = admin()) {
      assertThat(visitor.get(PATH + room.getId() + "/stations").statusCode()).isEqualTo(401);
      assertThat(visitor.json("PUT", path, request(), visitor.csrf()).statusCode()).isEqualTo(401);
      assertThat(client.json("PUT", path, request(), null).statusCode()).isEqualTo(403);
    }
    var manager =
        new UserAccount(
            "Gestor", "manager@example.com", passwords.encode(PASSWORD), AccessRole.ADMIN);
    manager.replacePermissions(Set.of(AdminPermission.MANAGE_ACCESS));
    accounts.saveAndFlush(manager);
    try (var client = login("manager@example.com")) {
      assertThat(client.get(PATH + room.getId() + "/stations").statusCode()).isEqualTo(403);
      assertThat(client.json("PUT", path, request(), client.csrf()).statusCode()).isEqualTo(403);
    }
    assertThat(spaces.findById(station.getId()).orElseThrow().getRoomId()).isNull();
  }

  private Map<String, Object> request() {
    return Map.of("roomId", room.getId(), "number", 2, "x", 25.5, "y", 40);
  }

  private StationPlacementRequest placement(int number) {
    return new StationPlacementRequest(
        room.getId(), number, new java.math.BigDecimal("25.50"), new java.math.BigDecimal("40.00"));
  }

  private SessionClient admin() throws Exception {
    return login("layout@example.com");
  }

  private SessionClient login(String email) throws Exception {
    var client = new SessionClient(port);
    assertThat(
            client
                .post(
                    "/api/v1/admin/auth/login",
                    Map.of("email", email, "password", PASSWORD),
                    client.csrf())
                .statusCode())
        .isEqualTo(204);
    return client;
  }
}
