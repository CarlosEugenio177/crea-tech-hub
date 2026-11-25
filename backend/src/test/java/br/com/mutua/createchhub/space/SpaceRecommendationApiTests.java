package br.com.mutua.createchhub.space;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.mutua.createchhub.block.SpaceBlockRepository;
import br.com.mutua.createchhub.support.PostgresTestConfiguration;
import br.com.mutua.createchhub.support.SessionClient;
import br.com.mutua.createchhub.user.AccessRole;
import br.com.mutua.createchhub.user.AdminPermission;
import br.com.mutua.createchhub.user.UserAccount;
import br.com.mutua.createchhub.user.UserAccountRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(PostgresTestConfiguration.class)
class SpaceRecommendationApiTests {

  private static final String PATH = "/api/v1/spaces/recommendations";
  private static final String BLOCKS_PATH = "/api/v1/admin/space-blocks";
  private static final String RESERVATIONS_PATH = "/api/v1/reservations";
  private static final String ADMIN_EMAIL = "recommend-admin@example.com";
  private static final String PASSWORD = "Recommend-test-password";

  private final JsonMapper json = JsonMapper.builder().build();

  @LocalServerPort private int port;
  @Autowired private SpaceRepository spaces;
  @Autowired private SpaceService spaceService;
  @Autowired private StationLayoutService layout;
  @Autowired private SpaceBlockRepository blocks;
  @Autowired private UserAccountRepository accounts;
  @Autowired private PasswordEncoder passwords;
  @Autowired private JdbcTemplate jdbc;

  @BeforeEach
  void prepare() {
    var account =
        new UserAccount("Operador", ADMIN_EMAIL, passwords.encode(PASSWORD), AccessRole.ADMIN);
    account.replacePermissions(Set.of(AdminPermission.MANAGE_SPACES));
    accounts.saveAndFlush(account);
  }

  @AfterEach
  void cleanup() {
    jdbc.update("DELETE FROM reservations");
    blocks.deleteAllInBatch();
    spaces.deleteAllInBatch();
    accounts.deleteAll();
  }

  @Test
  void recommendsSmallestSufficientSpaceAndListsAlternativesInCapacityOrder() throws Exception {
    var workstation =
        spaces.saveAndFlush(new Space("Estação Avulsa", SpaceType.WORKSTATION, 1, null));
    var coworking = spaces.saveAndFlush(new Space("Coworking", SpaceType.ROOM, 16, null));
    spaceService.changeBookingMode(coworking.getId(), BookingMode.PER_STATION);
    layout.generate(coworking.getId(), new StationBatchRequest(2, 2));

    var podcast = spaces.saveAndFlush(new Space("Sala de Podcast", SpaceType.ROOM, 4, null));
    var table =
        spaces.saveAndFlush(new Space("Mesa Colaborativa", SpaceType.SHARED_TABLE, 6, null));
    var meeting = spaces.saveAndFlush(new Space("Sala de Reunião", SpaceType.ROOM, 10, null));
    var auditorium = spaces.saveAndFlush(new Space("Auditório", SpaceType.ROOM, 80, null));

    var inactive = spaces.saveAndFlush(new Space("Sala Inativa", SpaceType.ROOM, 5, null));
    spaceService.changeStatus(inactive.getId(), false);

    try (var visitor = new SessionClient(port)) {
      var groupResponse =
          visitor.get(PATH + "?participants=5&startsAt=2099-03-10T09:00&endsAt=2099-03-10T10:00");
      assertThat(groupResponse.statusCode()).isEqualTo(200);
      assertThat(groupResponse.headers().firstValue("Cache-Control").orElse(""))
          .contains("no-store");

      var groupBody = json.readTree(groupResponse.body());
      assertThat(groupBody.path("status").asString()).isEqualTo("AVAILABLE");
      assertThat(groupBody.path("message").isMissingNode()).isTrue();
      assertThat(groupBody.path("recommended").path("id").asString())
          .isEqualTo(table.getId().toString());
      assertThat(ids(groupBody.path("spaces")))
          .containsExactly(
              table.getId().toString(), meeting.getId().toString(), auditorium.getId().toString());

      var singleResponse =
          visitor.get(PATH + "?participants=1&startsAt=2099-03-10T09:00&endsAt=2099-03-10T10:00");
      assertThat(singleResponse.statusCode()).isEqualTo(200);
      var singleBody = json.readTree(singleResponse.body());
      assertThat(singleBody.path("status").asString()).isEqualTo("AVAILABLE");
      assertThat(singleBody.path("recommended").path("id").asString())
          .isEqualTo(coworking.getId().toString());
      assertThat(ids(singleBody.path("spaces")))
          .containsExactly(
              coworking.getId().toString(),
              workstation.getId().toString(),
              podcast.getId().toString(),
              table.getId().toString(),
              meeting.getId().toString(),
              auditorium.getId().toString());
    }
  }

  @Test
  void excludesBlockedAndReservedSpacesWithoutLeakingInternalData() throws Exception {
    var table =
        spaces.saveAndFlush(new Space("Mesa Colaborativa", SpaceType.SHARED_TABLE, 6, null));
    var meeting = spaces.saveAndFlush(new Space("Sala de Reunião", SpaceType.ROOM, 10, null));

    try (var admin = admin();
        var visitor = new SessionClient(port)) {
      var adminCsrf = admin.csrf();
      var createdBlock =
          admin.json(
              "POST",
              BLOCKS_PATH,
              Map.of(
                  "spaceId",
                  table.getId(),
                  "startsAt",
                  "2099-03-10T09:30",
                  "endsAt",
                  "2099-03-10T10:00",
                  "reason",
                  "Manutenção reservada"),
              adminCsrf);
      assertThat(createdBlock.statusCode()).isEqualTo(201);
      var blockId = json.readTree(createdBlock.body()).path("id").asString();

      var afterBlock =
          visitor.get(PATH + "?participants=5&startsAt=2099-03-10T09:00&endsAt=2099-03-10T10:00");
      assertThat(afterBlock.statusCode()).isEqualTo(200);
      assertThat(afterBlock.body()).doesNotContain("Manutenção reservada", "createdBy");
      var afterBlockBody = json.readTree(afterBlock.body());
      assertThat(afterBlockBody.path("recommended").path("id").asString())
          .isEqualTo(meeting.getId().toString());
      assertThat(ids(afterBlockBody.path("spaces"))).containsExactly(meeting.getId().toString());

      var adjacent =
          json.readTree(
              visitor
                  .get(PATH + "?participants=5&startsAt=2099-03-10T10:00&endsAt=2099-03-10T11:00")
                  .body());
      assertThat(adjacent.path("recommended").path("id").asString())
          .isEqualTo(table.getId().toString());

      var visitorCsrf = visitor.csrf();
      assertThat(
              visitor
                  .json(
                      "POST",
                      RESERVATIONS_PATH,
                      reservationBody(meeting.getId(), null),
                      visitorCsrf)
                  .statusCode())
          .isEqualTo(201);

      var allBusy =
          visitor.get(PATH + "?participants=5&startsAt=2099-03-10T09:00&endsAt=2099-03-10T10:00");
      assertThat(allBusy.statusCode()).isEqualTo(200);
      assertThat(allBusy.body()).doesNotContain("52998224725", "visitor@example.com");
      var allBusyBody = json.readTree(allBusy.body());
      assertThat(allBusyBody.path("status").asString()).isEqualTo("UNAVAILABLE");
      assertThat(allBusyBody.path("recommended").isMissingNode()).isTrue();
      assertThat(allBusyBody.path("spaces").size()).isZero();
      assertThat(allBusyBody.path("message").asString())
          .contains("Ajuste a data, o horário ou o número de pessoas.");

      assertThat(
              admin
                  .json("POST", BLOCKS_PATH + "/" + blockId + "/cancel", null, adminCsrf)
                  .statusCode())
          .isEqualTo(200);

      var afterCancel =
          json.readTree(
              visitor
                  .get(PATH + "?participants=5&startsAt=2099-03-10T09:00&endsAt=2099-03-10T10:00")
                  .body());
      assertThat(afterCancel.path("status").asString()).isEqualTo("AVAILABLE");
      assertThat(afterCancel.path("recommended").path("id").asString())
          .isEqualTo(table.getId().toString());
    }
  }

  @Test
  void recommendsPerStationRoomOnlyWhileAtLeastOneActiveStationIsFreeAndRoomIsUnblocked()
      throws Exception {
    var emptyPerStation = spaces.saveAndFlush(new Space("Estudos Vazio", SpaceType.ROOM, 11, null));
    spaceService.changeBookingMode(emptyPerStation.getId(), BookingMode.PER_STATION);

    var coworking = spaces.saveAndFlush(new Space("Coworking", SpaceType.ROOM, 16, null));
    spaceService.changeBookingMode(coworking.getId(), BookingMode.PER_STATION);
    var stations = layout.generate(coworking.getId(), new StationBatchRequest(2, 2));

    var podcast = spaces.saveAndFlush(new Space("Sala de Podcast", SpaceType.ROOM, 4, null));

    try (var admin = admin();
        var visitor = new SessionClient(port)) {
      var visitorCsrf = visitor.csrf();
      assertThat(
              visitor
                  .json(
                      "POST",
                      RESERVATIONS_PATH,
                      reservationBody(coworking.getId(), stations.get(0).id()),
                      visitorCsrf)
                  .statusCode())
          .isEqualTo(201);

      var oneStationFree =
          json.readTree(
              visitor
                  .get(PATH + "?participants=1&startsAt=2099-03-10T09:00&endsAt=2099-03-10T10:00")
                  .body());
      assertThat(oneStationFree.path("recommended").path("id").asString())
          .isEqualTo(coworking.getId().toString());
      assertThat(ids(oneStationFree.path("spaces")))
          .containsExactly(coworking.getId().toString(), podcast.getId().toString());

      var adminCsrf = admin.csrf();
      var secondStationBlock =
          admin.json(
              "POST",
              BLOCKS_PATH,
              Map.of(
                  "spaceId",
                  stations.get(1).id(),
                  "startsAt",
                  "2099-03-10T09:00",
                  "endsAt",
                  "2099-03-10T10:00",
                  "reason",
                  "Manutenção estação 2"),
              adminCsrf);
      assertThat(secondStationBlock.statusCode()).isEqualTo(201);
      var secondBlockId = json.readTree(secondStationBlock.body()).path("id").asString();

      var zeroStationsFree =
          json.readTree(
              visitor
                  .get(PATH + "?participants=1&startsAt=2099-03-10T09:00&endsAt=2099-03-10T10:00")
                  .body());
      assertThat(zeroStationsFree.path("recommended").path("id").asString())
          .isEqualTo(podcast.getId().toString());
      assertThat(ids(zeroStationsFree.path("spaces"))).containsExactly(podcast.getId().toString());

      assertThat(
              admin
                  .json("POST", BLOCKS_PATH + "/" + secondBlockId + "/cancel", null, adminCsrf)
                  .statusCode())
          .isEqualTo(200);
      assertThat(
              admin
                  .json(
                      "POST",
                      BLOCKS_PATH,
                      Map.of(
                          "spaceId",
                          coworking.getId(),
                          "startsAt",
                          "2099-03-10T09:00",
                          "endsAt",
                          "2099-03-10T10:00",
                          "reason",
                          "Bloqueio da sala"),
                      adminCsrf)
                  .statusCode())
          .isEqualTo(201);

      var roomBlocked =
          json.readTree(
              visitor
                  .get(PATH + "?participants=1&startsAt=2099-03-10T09:00&endsAt=2099-03-10T10:00")
                  .body());
      assertThat(roomBlocked.path("recommended").path("id").asString())
          .isEqualTo(podcast.getId().toString());
    }
  }

  @Test
  void informsClearlyWhenCapacityExceedsAllSpacesOrHubIsClosed() throws Exception {
    spaces.saveAndFlush(new Space("Auditório", SpaceType.ROOM, 50, null));

    try (var visitor = new SessionClient(port)) {
      var tooMany =
          json.readTree(
              visitor
                  .get(PATH + "?participants=120&startsAt=2099-03-10T09:00&endsAt=2099-03-10T10:00")
                  .body());
      assertThat(tooMany.path("status").asString()).isEqualTo("UNAVAILABLE");
      assertThat(tooMany.path("recommended").isMissingNode()).isTrue();
      assertThat(tooMany.path("spaces").size()).isZero();
      assertThat(tooMany.path("message").asString())
          .contains("Ajuste a data, o horário ou o número de pessoas.");

      var closedHour =
          json.readTree(
              visitor
                  .get(PATH + "?participants=10&startsAt=2099-03-10T07:00&endsAt=2099-03-10T08:30")
                  .body());
      assertThat(closedHour.path("status").asString()).isEqualTo("CLOSED");
      assertThat(closedHour.path("recommended").isMissingNode()).isTrue();
      assertThat(closedHour.path("spaces").size()).isZero();
      assertThat(closedHour.path("message").asString()).contains("Ajuste a data ou o horário.");

      var closedWeekend =
          json.readTree(
              visitor
                  .get(PATH + "?participants=10&startsAt=2099-03-14T09:00&endsAt=2099-03-14T10:00")
                  .body());
      assertThat(closedWeekend.path("status").asString()).isEqualTo("CLOSED");
    }
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "startsAt=2099-03-10T09:00&endsAt=2099-03-10T10:00",
        "participants=0&startsAt=2099-03-10T09:00&endsAt=2099-03-10T10:00",
        "participants=-2&startsAt=2099-03-10T09:00&endsAt=2099-03-10T10:00",
        "participants=abc&startsAt=2099-03-10T09:00&endsAt=2099-03-10T10:00",
        "participants=2&endsAt=2099-03-10T10:00",
        "participants=2&startsAt=2099-03-10T09:00",
        "participants=2&startsAt=2099-03-10T09:00:00&endsAt=2099-03-10T10:00",
        "participants=2&startsAt=2099-02-31T09:00&endsAt=2099-02-31T10:00",
        "participants=2&startsAt=2099-03-10T10:00&endsAt=2099-03-10T10:00",
        "participants=2&startsAt=2099-03-10T11:00&endsAt=2099-03-10T10:00",
        "participants=2&startsAt=2020-03-10T09:00&endsAt=2020-03-10T10:00"
      })
  void rejectsInvalidParametersWithProblemDetail(String query) throws Exception {
    try (var visitor = new SessionClient(port)) {
      var response = visitor.get(PATH + "?" + query);
      assertThat(response.statusCode()).isEqualTo(400);
      assertThat(response.headers().firstValue("Content-Type").orElseThrow())
          .contains("application/problem+json");
    }
  }

  private List<String> ids(JsonNode array) {
    var result = new ArrayList<String>();
    for (var item : array) {
      result.add(item.path("id").asString());
    }
    return result;
  }

  private Map<String, Object> reservationBody(UUID spaceId, UUID stationId) {
    if (stationId == null) {
      return Map.of(
          "spaceId",
          spaceId,
          "startsAt",
          "2099-03-10T09:00",
          "endsAt",
          "2099-03-10T10:00",
          "requester",
          requester(),
          "rulesAccepted",
          true);
    }
    return Map.of(
        "spaceId",
        spaceId,
        "stationId",
        stationId,
        "startsAt",
        "2099-03-10T09:00",
        "endsAt",
        "2099-03-10T10:00",
        "requester",
        requester(),
        "rulesAccepted",
        true);
  }

  private Map<String, Object> requester() {
    return Map.of(
        "cpf",
        "52998224725",
        "fullName",
        "Visitante de Teste",
        "email",
        "visitor@example.com",
        "whatsapp",
        "86912345678",
        "profile",
        "EXTERNAL",
        "onBehalfOfOthers",
        false,
        "reason",
        "Reunião de projeto",
        "notes",
        "");
  }

  private SessionClient admin() throws Exception {
    var client = new SessionClient(port);
    assertThat(
            client
                .post(
                    "/api/v1/admin/auth/login",
                    Map.of("email", ADMIN_EMAIL, "password", PASSWORD),
                    client.csrf())
                .statusCode())
        .isEqualTo(204);
    return client;
  }
}
