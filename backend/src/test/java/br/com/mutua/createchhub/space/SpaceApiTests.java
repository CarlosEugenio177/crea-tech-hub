package br.com.mutua.createchhub.space;

import static org.assertj.core.api.Assertions.assertThat;

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
import org.junit.jupiter.params.provider.CsvSource;
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
class SpaceApiTests {

  private static final String PATH = "/api/v1/admin/spaces";
  private static final String PASSWORD = "Space-admin-test-password";
  private final JsonMapper json = JsonMapper.builder().build();

  @LocalServerPort private int port;
  @Autowired private SpaceRepository spaces;
  @Autowired private UserAccountRepository accounts;
  @Autowired private PasswordEncoder passwords;

  @BeforeEach
  void createAdmin() {
    var account =
        new UserAccount(
            "Admin de espaços", "spaces@example.com", passwords.encode(PASSWORD), AccessRole.ADMIN);
    account.replacePermissions(Set.of(AdminPermission.MANAGE_SPACES));
    accounts.saveAndFlush(account);
  }

  @AfterEach
  void cleanUp() {
    spaces.deleteAll();
    accounts.deleteAll();
  }

  @Test
  void createsEditsDeactivatesAndReactivatesWithoutDeletingTheSpace() throws Exception {
    try (var client = admin()) {
      var csrf = client.csrf();
      var response =
          client.json(
              "POST",
              PATH,
              Map.of(
                  "name",
                  " Sala de reunião ",
                  "type",
                  "ROOM",
                  "capacity",
                  8,
                  "description",
                  " Com projetor. "),
              csrf);
      assertThat(response.statusCode()).isEqualTo(201);
      var created = json.readTree(response.body());
      var id = created.path("id").asString();
      var itemPath = PATH + "/" + id;
      assertThat(response.headers().firstValue("Location").orElseThrow()).endsWith(itemPath);
      assertThat(created.path("name").asString()).isEqualTo("Sala de reunião");
      assertThat(created.path("description").asString()).isEqualTo("Com projetor.");
      assertThat(created.path("active").asBoolean()).isTrue();
      assertThat(created.path("createdAt").asString()).isNotBlank();

      assertThat(
              client
                  .json("PATCH", itemPath + "/status", Map.of("active", false), csrf)
                  .statusCode())
          .isEqualTo(200);
      var edited =
          client.json(
              "PUT",
              itemPath,
              Map.of("name", "Sala revisada", "type", "ROOM", "capacity", 10),
              csrf);
      assertThat(edited.statusCode()).isEqualTo(200);
      var body = json.readTree(edited.body());
      assertThat(body.path("active").asBoolean()).isFalse();
      assertThat(body.path("capacity").asInt()).isEqualTo(10);
      assertThat(body.path("description").isNull()).isTrue();
      assertThat(body.path("createdAt")).isEqualTo(created.path("createdAt"));

      var loaded = client.get(itemPath);
      assertThat(loaded.statusCode()).isEqualTo(200);
      assertThat(json.readTree(loaded.body()).path("name").asString()).isEqualTo("Sala revisada");
      assertThat(
              client.json("PATCH", itemPath + "/status", Map.of("active", true), csrf).statusCode())
          .isEqualTo(200);
      assertThat(spaces.findById(UUID.fromString(id)).orElseThrow().isActive()).isTrue();
      assertThat(spaces.count()).isEqualTo(1);
      assertThat(client.json("DELETE", itemPath, Map.of(), csrf).statusCode()).isEqualTo(405);
      assertThat(spaces.existsById(UUID.fromString(id))).isTrue();
    }
  }

  @ParameterizedTest
  @CsvSource({"WORKSTATION,1", "SHARED_TABLE,6"})
  void createsEachKindOfReservableUnit(String type, int capacity) throws Exception {
    try (var client = admin()) {
      var response =
          client.json(
              "POST",
              PATH,
              Map.of("name", "Unidade 01", "type", type, "capacity", capacity),
              client.csrf());
      assertThat(response.statusCode()).isEqualTo(201);
      var body = json.readTree(response.body());
      assertThat(body.path("type").asString()).isEqualTo(type);
      assertThat(body.path("capacity").asInt()).isEqualTo(capacity);
    }
  }

  @Test
  void paginatesInStableOrderAndFiltersActiveSpaces() throws Exception {
    spaces.saveAndFlush(new Space("Sala B", SpaceType.ROOM, 4, null));
    var inactive = new Space("Sala A", SpaceType.ROOM, 2, null);
    inactive.changeStatus(false);
    spaces.saveAndFlush(inactive);
    spaces.saveAndFlush(new Space("Sala C", SpaceType.ROOM, 8, null));
    try (var client = admin()) {
      var first = json.readTree(client.get(PATH + "?page=0&size=2").body());
      assertThat(first.path("items").size()).isEqualTo(2);
      assertThat(first.path("items").path(0).path("name").asString()).isEqualTo("Sala A");
      assertThat(first.path("items").path(1).path("name").asString()).isEqualTo("Sala B");
      assertThat(first.path("totalElements").asLong()).isEqualTo(3);
      assertThat(first.path("totalPages").asInt()).isEqualTo(2);
      var second = json.readTree(client.get(PATH + "?page=1&size=2").body());
      assertThat(second.path("page").asInt()).isEqualTo(1);
      assertThat(second.path("items").size()).isEqualTo(1);
      assertThat(second.path("items").path(0).path("name").asString()).isEqualTo("Sala C");
      var activeOnly = json.readTree(client.get(PATH + "?active=true").body());
      assertThat(activeOnly.path("totalElements").asLong()).isEqualTo(2);
      var inactiveOnly = json.readTree(client.get(PATH + "?active=false").body());
      assertThat(inactiveOnly.path("totalElements").asLong()).isEqualTo(1);
      assertThat(inactiveOnly.path("items").path(0).path("id").asString())
          .isEqualTo(inactive.getId().toString());
    }
  }

  @ParameterizedTest
  @MethodSource("invalidFields")
  void rejectsInvalidInputWithoutPersistingAnything(String field, Object value) throws Exception {
    var request =
        new HashMap<String, Object>(Map.of("name", "Sala teste", "type", "ROOM", "capacity", 4));
    request.put(field, value);
    try (var client = admin()) {
      var response = client.json("POST", PATH, request, client.csrf());
      assertThat(response.statusCode()).isEqualTo(400);
      assertThat(response.headers().firstValue("Content-Type").orElseThrow())
          .contains("application/problem+json");
      assertThat(spaces.count()).isZero();
    }
  }

  static Stream<Arguments> invalidFields() {
    return Stream.of(
        Arguments.of("name", "   "),
        Arguments.of("name", "x".repeat(151)),
        Arguments.of("name", null),
        Arguments.of("type", null),
        Arguments.of("type", "UNKNOWN"),
        Arguments.of("type", 0),
        Arguments.of("capacity", null),
        Arguments.of("capacity", 0),
        Arguments.of("capacity", -1),
        Arguments.of("capacity", 1.5),
        Arguments.of("description", "x".repeat(2001)));
  }

  @Test
  void rejectsMultiplePeopleForAnIndividualWorkstation() throws Exception {
    try (var client = admin()) {
      var response =
          client.json(
              "POST",
              PATH,
              Map.of("name", "Estação 01", "type", "WORKSTATION", "capacity", 2),
              client.csrf());
      assertThat(response.statusCode()).isEqualTo(400);
      assertThat(json.readTree(response.body()).path("errors").toString())
          .contains("capacityCompatible");
      assertThat(spaces.count()).isZero();
    }
  }

  @ParameterizedTest
  @ValueSource(strings = {"page=-1", "size=0", "size=101", "active=unknown", "page=abc"})
  void rejectsInvalidPaginationAndFilters(String query) throws Exception {
    try (var client = admin()) {
      var response = client.get(PATH + "?" + query);
      assertThat(response.statusCode()).isEqualTo(400);
      assertThat(response.headers().firstValue("Content-Type").orElseThrow())
          .contains("application/problem+json");
    }
  }

  @Test
  void returnsNotFoundForUnknownSpacesAndBadRequestForInvalidIds() throws Exception {
    try (var client = admin()) {
      var path = PATH + "/" + UUID.randomUUID();
      var csrf = client.csrf();
      assertThat(client.get(path).statusCode()).isEqualTo(404);
      assertThat(
              client
                  .json("PUT", path, Map.of("name", "Sala", "type", "ROOM", "capacity", 2), csrf)
                  .statusCode())
          .isEqualTo(404);
      assertThat(client.json("PATCH", path + "/status", Map.of("active", false), csrf).statusCode())
          .isEqualTo(404);
      assertThat(client.get(PATH + "/invalid-id").statusCode()).isEqualTo(400);
    }
  }

  @Test
  void rejectsMissingStatusWithoutChangingTheSpace() throws Exception {
    var space = spaces.saveAndFlush(new Space("Sala", SpaceType.ROOM, 2, null));
    try (var client = admin()) {
      assertThat(
              client
                  .json("PATCH", PATH + "/" + space.getId() + "/status", Map.of(), client.csrf())
                  .statusCode())
          .isEqualTo(400);
      assertThat(spaces.findById(space.getId()).orElseThrow().isActive()).isTrue();
    }
  }

  @Test
  void rejectsStatusWithANumericValue() throws Exception {
    var space = spaces.saveAndFlush(new Space("Sala", SpaceType.ROOM, 2, null));
    try (var client = admin()) {
      assertThat(
              client
                  .json(
                      "PATCH",
                      PATH + "/" + space.getId() + "/status",
                      Map.of("active", 0),
                      client.csrf())
                  .statusCode())
          .isEqualTo(400);
      assertThat(spaces.findById(space.getId()).orElseThrow().isActive()).isTrue();
    }
  }

  @Test
  void refusesWritesWithoutCsrfAndDoesNotChangeData() throws Exception {
    var space = spaces.saveAndFlush(new Space("Sala", SpaceType.ROOM, 2, null));
    try (var client = admin()) {
      var request = Map.of("name", "Alterado", "type", "ROOM", "capacity", 4);
      assertThat(client.json("POST", PATH, request, null).statusCode()).isEqualTo(403);
      assertThat(client.json("PUT", PATH + "/" + space.getId(), request, null).statusCode())
          .isEqualTo(403);
      assertThat(
              client
                  .json(
                      "PATCH",
                      PATH + "/" + space.getId() + "/status",
                      Map.of("active", false),
                      null)
                  .statusCode())
          .isEqualTo(403);
      var stored = spaces.findById(space.getId()).orElseThrow();
      assertThat(stored.getName()).isEqualTo("Sala");
      assertThat(stored.isActive()).isTrue();
      assertThat(spaces.count()).isEqualTo(1);
    }
  }

  @Test
  void listsOnlyActiveSpacesInThePublicCatalogWithoutAuthentication() throws Exception {
    var active = spaces.saveAndFlush(new Space("Sala aberta", SpaceType.ROOM, 8, "Projetor"));
    var inactive = new Space("Sala fechada", SpaceType.ROOM, 4, null);
    inactive.changeStatus(false);
    spaces.saveAndFlush(inactive);

    try (var client = new SessionClient(port)) {
      assertThat(client.get(PATH).statusCode()).isEqualTo(401);
      assertThat(client.get(PATH + "/" + UUID.randomUUID()).statusCode()).isEqualTo(401);
      assertThat(
              client
                  .json(
                      "POST",
                      PATH,
                      Map.of("name", "Sala", "type", "ROOM", "capacity", 4),
                      client.csrf())
                  .statusCode())
          .isEqualTo(401);
      var catalog = client.get("/api/v1/spaces?page=0&size=20");
      assertThat(catalog.statusCode()).isEqualTo(200);
      var body = json.readTree(catalog.body());
      assertThat(body.path("totalElements").asLong()).isEqualTo(1);
      assertThat(body.path("items").path(0).path("id").asString())
          .isEqualTo(active.getId().toString());
      assertThat(body.path("items").path(0).path("description").asText()).isEqualTo("Projetor");
      assertThat(body.path("items").path(0).has("active")).isFalse();
      assertThat(spaces.count()).isEqualTo(2);
    }
  }

  @Test
  void paginatesThePublicCatalogInStableNameOrder() throws Exception {
    spaces.saveAndFlush(new Space("Sala B", SpaceType.ROOM, 4, null));
    spaces.saveAndFlush(new Space("Sala A", SpaceType.ROOM, 8, null));
    var inactive = new Space("Sala C", SpaceType.ROOM, 6, null);
    inactive.changeStatus(false);
    spaces.saveAndFlush(inactive);

    try (var client = new SessionClient(port)) {
      var first = json.readTree(client.get("/api/v1/spaces?page=0&size=1").body());
      var second = json.readTree(client.get("/api/v1/spaces?page=1&size=1").body());

      assertThat(first.path("items").path(0).path("name").asString()).isEqualTo("Sala A");
      assertThat(second.path("items").path(0).path("name").asString()).isEqualTo("Sala B");
      assertThat(first.path("totalElements").asLong()).isEqualTo(2);
      assertThat(first.path("totalPages").asInt()).isEqualTo(2);
    }
  }

  @ParameterizedTest
  @ValueSource(strings = {"page=-1", "size=0", "size=101", "page=abc"})
  void rejectsInvalidPublicCatalogPagination(String query) throws Exception {
    try (var client = new SessionClient(port)) {
      assertThat(client.get("/api/v1/spaces?" + query).statusCode()).isEqualTo(400);
    }
  }

  private SessionClient admin() throws Exception {
    var client = new SessionClient(port);
    var response =
        client.post(
            "/api/v1/admin/auth/login",
            Map.of("email", "spaces@example.com", "password", PASSWORD),
            client.csrf());
    assertThat(response.statusCode()).isEqualTo(204);
    return client;
  }
}
