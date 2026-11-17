package br.com.mutua.createchhub.block;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.mutua.createchhub.space.Space;
import br.com.mutua.createchhub.space.SpaceRepository;
import br.com.mutua.createchhub.space.SpaceService;
import br.com.mutua.createchhub.space.SpaceType;
import br.com.mutua.createchhub.space.StationLayoutService;
import br.com.mutua.createchhub.space.StationPlacementRequest;
import br.com.mutua.createchhub.support.PostgresTestConfiguration;
import br.com.mutua.createchhub.support.SessionClient;
import br.com.mutua.createchhub.user.AccessRole;
import br.com.mutua.createchhub.user.AdminPermission;
import br.com.mutua.createchhub.user.UserAccount;
import br.com.mutua.createchhub.user.UserAccountRepository;
import java.math.BigDecimal;
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
class SpaceBlockApiTests {

  private static final String PATH = "/api/v1/admin/space-blocks";
  private static final String PASSWORD = "Space-block-test-password";
  private final JsonMapper json = JsonMapper.builder().build();
  @LocalServerPort private int port;
  @Autowired private SpaceBlockRepository blocks;
  @Autowired private SpaceBlockService service;
  @Autowired private SpaceRepository spaces;
  @Autowired private SpaceService spaceService;
  @Autowired private StationLayoutService layout;
  @Autowired private UserAccountRepository accounts;
  @Autowired private PasswordEncoder passwords;
  private UUID actor;
  private UUID room;

  @BeforeEach
  void prepare() {
    room = spaces.saveAndFlush(new Space("Sala", SpaceType.ROOM, 12, null)).getId();
    var account =
        new UserAccount(
            "Operador", "blocks@example.com", passwords.encode(PASSWORD), AccessRole.ADMIN);
    account.replacePermissions(Set.of(AdminPermission.MANAGE_SPACES));
    actor = accounts.saveAndFlush(account).getId();
  }

  @AfterEach
  void cleanup() {
    blocks.deleteAllInBatch();
    spaces.deleteAllInBatch();
    accounts.deleteAll();
  }

  @Test
  void createsAndCancelsWithoutDeletingOrChangingTheOriginalRecord() throws Exception {
    try (var client = admin()) {
      var response = client.json("POST", PATH, request(), client.csrf());
      assertThat(response.statusCode()).isEqualTo(201);
      var created = json.readTree(response.body());
      var path = PATH + "/" + created.path("id").asString();
      assertThat(response.headers().firstValue("Location").orElseThrow()).endsWith(path);
      assertThat(created.path("startsAt").asString()).isEqualTo("2027-03-10T09:15");
      assertThat(created.path("reason").asString()).isEqualTo("Manutenção");
      assertThat(created.path("createdBy").asString()).isEqualTo(actor.toString());
      assertThat(created.path("active").asBoolean()).isTrue();
      assertThat(created.has("cancelledAt")).isFalse();
      assertThat(json.readTree(client.get(path).body())).isEqualTo(created);

      var cancelled = client.json("POST", path + "/cancel", Map.of(), client.csrf());
      assertThat(cancelled.statusCode()).isEqualTo(200);
      var body = json.readTree(cancelled.body());
      assertThat(body.path("active").asBoolean()).isFalse();
      assertThat(body.path("cancelledBy").asString()).isEqualTo(actor.toString());
      assertThat(body.path("cancelledAt").asString()).isNotBlank();
      assertThat(body.path("createdAt")).isEqualTo(created.path("createdAt"));
      assertThat(body.path("startsAt")).isEqualTo(created.path("startsAt"));
      assertThat(
              json.readTree(client.json("POST", path + "/cancel", Map.of(), client.csrf()).body()))
          .isEqualTo(body);
      assertThat(
              json.readTree(client.get(PATH + "?active=true").body()).path("totalElements").asInt())
          .isZero();
      assertThat(
              json.readTree(client.get(PATH + "?active=false").body())
                  .path("totalElements")
                  .asInt())
          .isEqualTo(1);
      assertThat(json.readTree(client.get(PATH).body()).path("totalElements").asInt()).isEqualTo(1);
      assertThat(client.json("DELETE", path, Map.of(), client.csrf()).statusCode()).isEqualTo(405);
      assertThat(blocks.count()).isEqualTo(1);
    }
  }

  @Test
  void inheritsRoomBlocksWithoutCopyingThemOrAffectingSiblingStations() throws Exception {
    var first = station(1);
    var second = station(2);
    var roomBlock = create(room, "09:00", "12:00");
    create(first, "10:00", "11:00");
    try (var client = admin()) {
      assertThat(count(client, first, true)).isEqualTo(2);
      assertThat(count(client, second, true)).isEqualTo(1);
      assertThat(count(client, room, true)).isEqualTo(1);
      service.cancel(roomBlock.id(), actor);
      assertThat(count(client, first, true)).isEqualTo(1);
      assertThat(count(client, second, true)).isZero();
      assertThat(count(client, second, false)).isEqualTo(1);
      assertThat(blocks.count()).isEqualTo(2);
    }
  }

  @Test
  void filtersByOverlapUsingExclusiveEndsAndPaginatesInStableOrder() throws Exception {
    var shortBlock = create(room, "09:00", "10:00");
    var longerBlock = create(room, "09:00", "11:00");
    create(room, "13:00", "14:00");
    var expectedFirst =
        Stream.of(shortBlock.id(), longerBlock.id())
            .map(UUID::toString)
            .sorted()
            .findFirst()
            .orElseThrow();
    try (var client = admin()) {
      var first = json.readTree(client.get(PATH + "?page=0&size=1").body());
      assertThat(first.path("items").path(0).path("id").asString()).isEqualTo(expectedFirst);
      assertThat(first.path("totalElements").asInt()).isEqualTo(3);
      assertThat(json.readTree(client.get(PATH + "?page=9&size=1").body()).path("items").size())
          .isZero();
      var overlaps =
          json.readTree(client.get(PATH + "?from=2027-03-10T10:00&to=2027-03-10T13:00").body());
      assertThat(overlaps.path("totalElements").asInt()).isEqualTo(1);
      assertThat(overlaps.path("items").path(0).path("id").asString())
          .isEqualTo(longerBlock.id().toString());
      assertThat(
              json.readTree(client.get(PATH + "?from=2027-03-10T14:00").body())
                  .path("totalElements")
                  .asInt())
          .isZero();
      assertThat(
              json.readTree(client.get(PATH + "?to=2027-03-10T09:00").body())
                  .path("totalElements")
                  .asInt())
          .isZero();
    }
  }

  @Test
  void letsTheAdministratorBlockInactiveSpacesAcrossDaysAndOutsideOpeningHours() throws Exception {
    spaceService.changeStatus(room, false);
    var request = new HashMap<>(request());
    request.put("startsAt", "2027-03-10T19:00");
    request.put("endsAt", "2027-03-12T06:00");
    try (var client = admin()) {
      assertThat(client.json("POST", PATH, request, client.csrf()).statusCode()).isEqualTo(201);
    }
  }

  @ParameterizedTest
  @MethodSource("invalidFields")
  void rejectsInvalidBlocksWithoutPersistingThem(String field, Object value) throws Exception {
    var request = new HashMap<>(request());
    request.put(field, value);
    try (var client = admin()) {
      var response = client.json("POST", PATH, request, client.csrf());
      assertThat(response.statusCode()).isEqualTo(400);
      assertThat(response.headers().firstValue("Content-Type").orElseThrow())
          .contains("application/problem+json");
      assertThat(blocks.count()).isZero();
    }
  }

  static Stream<Arguments> invalidFields() {
    return Stream.of(
        Arguments.of("spaceId", null),
        Arguments.of("spaceId", "invalid"),
        Arguments.of("startsAt", null),
        Arguments.of("startsAt", "2027-02-31T09:15"),
        Arguments.of("startsAt", "2027-03-10T09:15:30"),
        Arguments.of("startsAt", "2027-03-10T09:15Z"),
        Arguments.of("startsAt", "0000-03-10T09:15"),
        Arguments.of("startsAt", "2027-03-10T24:00"),
        Arguments.of("endsAt", null),
        Arguments.of("endsAt", "2027-03-10T09:15"),
        Arguments.of("endsAt", "2027-03-10T08:00"),
        Arguments.of("reason", "   "),
        Arguments.of("reason", "x".repeat(501)));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "page=-1",
        "size=0",
        "size=101",
        "active=unknown",
        "from=2027-02-31T10:00",
        "from=2027-03-10T09:00:01",
        "to=bad",
        "from=2027-03-10T11:00&to=2027-03-10T09:00"
      })
  void rejectsInvalidFilters(String query) throws Exception {
    try (var client = admin()) {
      assertThat(client.get(PATH + "?" + query).statusCode()).isEqualTo(400);
    }
  }

  @Test
  void returnsNotFoundForUnknownBlocksAndSpaces() throws Exception {
    try (var client = admin()) {
      assertThat(client.get(PATH + "/" + UUID.randomUUID()).statusCode()).isEqualTo(404);
      assertThat(
              client
                  .json("POST", PATH + "/" + UUID.randomUUID() + "/cancel", Map.of(), client.csrf())
                  .statusCode())
          .isEqualTo(404);
      assertThat(client.get(PATH + "?spaceId=" + UUID.randomUUID()).statusCode()).isEqualTo(404);
      var request = new HashMap<>(request());
      request.put("spaceId", UUID.randomUUID());
      assertThat(client.json("POST", PATH, request, client.csrf()).statusCode()).isEqualTo(404);
    }
  }

  @Test
  void requiresSpacePermissionAuthenticationAndCsrf() throws Exception {
    var block = create(room, "09:00", "10:00");
    var cancelPath = PATH + "/" + block.id() + "/cancel";
    try (var visitor = new SessionClient(port);
        var client = admin()) {
      assertThat(visitor.get(PATH).statusCode()).isEqualTo(401);
      assertThat(visitor.get(PATH + "/" + block.id()).statusCode()).isEqualTo(401);
      assertThat(visitor.json("POST", PATH, request(), visitor.csrf()).statusCode()).isEqualTo(401);
      assertThat(client.json("POST", PATH, request(), null).statusCode()).isEqualTo(403);
      assertThat(client.json("POST", cancelPath, Map.of(), null).statusCode()).isEqualTo(403);
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
      assertThat(client.get(PATH).statusCode()).isEqualTo(403);
      assertThat(client.json("POST", PATH, request(), client.csrf()).statusCode()).isEqualTo(403);
      assertThat(client.json("POST", cancelPath, Map.of(), client.csrf()).statusCode())
          .isEqualTo(403);
    }
    assertThat(service.find(block.id()).active()).isTrue();
    assertThat(blocks.count()).isEqualTo(1);
  }

  private Map<String, Object> request() {
    return Map.of(
        "spaceId",
        room,
        "startsAt",
        "2027-03-10T09:15",
        "endsAt",
        "2027-03-10T12:30",
        "reason",
        " Manutenção ");
  }

  private SpaceBlockResponse create(UUID space, String start, String end) {
    return service.create(
        new SpaceBlockRequest(space, "2027-03-10T" + start, "2027-03-10T" + end, "Manutenção"),
        actor);
  }

  private UUID station(int number) {
    var station = spaces.saveAndFlush(new Space("Estação", SpaceType.WORKSTATION, 1, null));
    layout.place(
        station.getId(),
        new StationPlacementRequest(room, number, BigDecimal.ZERO, BigDecimal.TEN));
    return station.getId();
  }

  private int count(SessionClient client, UUID space, boolean active) throws Exception {
    return json.readTree(client.get(PATH + "?spaceId=" + space + "&active=" + active).body())
        .path("totalElements")
        .asInt();
  }

  private SessionClient admin() throws Exception {
    var client = new SessionClient(port);
    assertThat(
            client
                .post(
                    "/api/v1/admin/auth/login",
                    Map.of("email", "blocks@example.com", "password", PASSWORD),
                    client.csrf())
                .statusCode())
        .isEqualTo(204);
    return client;
  }
}
