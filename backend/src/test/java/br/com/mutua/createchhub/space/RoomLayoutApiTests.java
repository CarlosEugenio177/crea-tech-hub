package br.com.mutua.createchhub.space;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.mutua.createchhub.support.PostgresTestConfiguration;
import br.com.mutua.createchhub.support.SessionClient;
import br.com.mutua.createchhub.user.AccessRole;
import br.com.mutua.createchhub.user.AdminPermission;
import br.com.mutua.createchhub.user.UserAccount;
import br.com.mutua.createchhub.user.UserAccountRepository;
import java.util.HashMap;
import java.util.List;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(PostgresTestConfiguration.class)
class RoomLayoutApiTests {

  private static final String PASSWORD = "Room-layout-test-password";
  private final JsonMapper json = JsonMapper.builder().build();
  @LocalServerPort private int port;
  @Autowired private SpaceRepository spaces;
  @Autowired private SpaceService service;
  @Autowired private StationLayoutService stations;
  @Autowired private UserAccountRepository accounts;
  @Autowired private PasswordEncoder passwords;
  private SpaceResponse room;
  private List<SpaceResponse> seats;

  @BeforeEach
  void prepare() {
    var created = spaces.saveAndFlush(new Space("Coworking", SpaceType.ROOM, 12, null));
    room = service.changeBookingMode(created.getId(), BookingMode.PER_STATION);
    seats = stations.generate(room.id(), new StationBatchRequest(3, 3));
    var admin =
        new UserAccount(
            "Operador", "room@example.com", passwords.encode(PASSWORD), AccessRole.ADMIN);
    admin.replacePermissions(Set.of(AdminPermission.MANAGE_SPACES));
    accounts.saveAndFlush(admin);
  }

  @AfterEach
  void cleanup() {
    spaces.deleteAllInBatch();
    accounts.deleteAll();
  }

  @Test
  void savesDimensionsPositionsAndRemovalsTogetherWithoutDeletingRecords() throws Exception {
    var body = request();
    body.put("positions", List.of(position(seats.getFirst(), 20)));
    body.put("removedStations", List.of(reference(seats.get(1))));
    try (var client = admin()) {
      var response = client.json("PUT", path(), body, client.csrf());
      assertThat(response.statusCode()).isEqualTo(200);
      var result = json.readTree(response.body());
      assertThat(result.path("roomDimensions").path("width").asDouble()).isEqualTo(8);
      assertThat(result.path("version").asLong()).isGreaterThan(room.version());
      assertThat(service.find(room.id()).roomDimensions().length()).isEqualByComparingTo("5");
      assertThat(service.find(seats.getFirst().id()).stationPlacement().x())
          .isEqualByComparingTo("20");
      var removed = spaces.findById(seats.get(1).id()).orElseThrow();
      assertThat(removed.isActive()).isFalse();
      assertThat(removed.getRoomId()).isNull();
      assertThat(removed.getStationNumber()).isNull();
      assertThat(spaces.count()).isEqualTo(4);
      assertThat(stations.list(room.id(), false, 0, 100).items()).hasSize(2);
      var publicSpaces = client.get("/api/v1/spaces?size=100");
      assertThat(publicSpaces.body()).doesNotContain(seats.get(1).id().toString());
    }
  }

  @Test
  void staleStationPreventsAllChangesIncludingResizingAndOtherRemovals() throws Exception {
    var changed = seats.getFirst();
    service.changeStatus(changed.id(), false);
    var body = request();
    body.put("positions", List.of(position(changed, 20)));
    body.put("removedStations", List.of(reference(seats.get(1))));
    try (var client = admin()) {
      assertThat(client.json("PUT", path(), body, client.csrf()).statusCode()).isEqualTo(409);
    }
    assertThat(service.find(room.id()).roomDimensions().width()).isEqualByComparingTo("10");
    assertThat(service.find(changed.id()).stationPlacement().x())
        .isEqualTo(changed.stationPlacement().x());
    assertThat(service.find(seats.get(1).id()).active()).isTrue();
  }

  @Test
  void rejectsStaleRoomAndStationsFromAnotherRoom() throws Exception {
    try (var client = admin()) {
      service.update(
          room.id(), new SpaceRequest("Renomeada", SpaceType.ROOM, 12, null, null, null));
      assertThat(client.json("PUT", path(), request(), client.csrf()).statusCode()).isEqualTo(409);
      room = service.find(room.id());
      var other = spaces.saveAndFlush(new Space("Outra", SpaceType.WORKSTATION, 1, null));
      var body = request();
      body.put(
          "removedStations", List.of(Map.of("id", other.getId(), "version", other.getVersion())));
      assertThat(client.json("PUT", path(), body, client.csrf()).statusCode()).isEqualTo(409);
      body.put("removedStations", List.of(Map.of("id", UUID.randomUUID(), "version", 0)));
      assertThat(client.json("PUT", path(), body, client.csrf()).statusCode()).isEqualTo(404);
      body.put("positions", List.of(position(seats.getFirst(), 20)));
      body.put("removedStations", List.of(reference(seats.getFirst())));
      assertThat(client.json("PUT", path(), body, client.csrf()).statusCode()).isEqualTo(400);
    }
  }

  @Test
  void canRemoveAllStationsAndThenChangeToWholeSpace() throws Exception {
    var body = request();
    body.put("removedStations", seats.stream().map(this::reference).toList());
    try (var client = admin()) {
      assertThat(client.json("PUT", path(), body, client.csrf()).statusCode()).isEqualTo(200);
      assertThat(stations.list(room.id(), false, 0, 100).items()).isEmpty();
      assertThat(service.changeBookingMode(room.id(), BookingMode.WHOLE_SPACE).bookingMode())
          .isEqualTo(BookingMode.WHOLE_SPACE);
      assertThat(client.json("PUT", path(), request(), client.csrf()).statusCode()).isEqualTo(409);
      assertThat(
              client
                  .json(
                      "PUT",
                      "/api/v1/admin/spaces/" + UUID.randomUUID() + "/layout",
                      request(),
                      client.csrf())
                  .statusCode())
          .isEqualTo(404);
    }
  }

  @ParameterizedTest
  @MethodSource("invalidFields")
  void rejectsInvalidPayloads(String field, Object value) throws Exception {
    var body = request();
    body.put(field, value);
    try (var client = admin()) {
      var response = client.json("PUT", path(), body, client.csrf());
      assertThat(response.statusCode()).isEqualTo(400);
      assertThat(response.headers().firstValue("Content-Type").orElseThrow())
          .contains("application/problem+json");
    }
    assertThat(service.find(room.id()).roomDimensions().width()).isEqualByComparingTo("10");
  }

  static Stream<Arguments> invalidFields() {
    return Stream.of(
        Arguments.of("width", 0),
        Arguments.of("width", 101),
        Arguments.of("width", 1.234),
        Arguments.of("length", null),
        Arguments.of("length", -1),
        Arguments.of("version", -1),
        Arguments.of("positions", null),
        Arguments.of("removedStations", null),
        Arguments.of("positions", java.util.Arrays.asList((Object) null)),
        Arguments.of("removedStations", List.of(Map.of("id", UUID.randomUUID(), "version", -1))));
  }

  @Test
  void requiresSessionCsrfAndManageSpaces() throws Exception {
    try (var visitor = new SessionClient(port);
        var client = admin()) {
      assertThat(visitor.json("PUT", path(), request(), visitor.csrf()).statusCode())
          .isEqualTo(401);
      assertThat(client.json("PUT", path(), request(), null).statusCode()).isEqualTo(403);
    }
    var manager =
        new UserAccount(
            "Gestor", "manager-room@example.com", passwords.encode(PASSWORD), AccessRole.ADMIN);
    manager.replacePermissions(Set.of(AdminPermission.MANAGE_ACCESS));
    accounts.saveAndFlush(manager);
    try (var client = new SessionClient(port)) {
      client.post(
          "/api/v1/admin/auth/login",
          Map.of("email", manager.getEmail(), "password", PASSWORD),
          client.csrf());
      assertThat(client.json("PUT", path(), request(), client.csrf()).statusCode()).isEqualTo(403);
    }
  }

  private HashMap<String, Object> request() {
    return new HashMap<>(
        Map.of(
            "version",
            room.version(),
            "width",
            8,
            "length",
            5,
            "positions",
            List.of(),
            "removedStations",
            List.of()));
  }

  private Map<String, Object> position(SpaceResponse station, int x) {
    return Map.of("id", station.id(), "version", station.version(), "x", x, "y", 50);
  }

  private Map<String, Object> reference(SpaceResponse station) {
    return Map.of("id", station.id(), "version", station.version());
  }

  private String path() {
    return "/api/v1/admin/spaces/" + room.id() + "/layout";
  }

  private SessionClient admin() throws Exception {
    var client = new SessionClient(port);
    assertThat(
            client
                .post(
                    "/api/v1/admin/auth/login",
                    Map.of("email", "room@example.com", "password", PASSWORD),
                    client.csrf())
                .statusCode())
        .isEqualTo(204);
    return client;
  }
}
