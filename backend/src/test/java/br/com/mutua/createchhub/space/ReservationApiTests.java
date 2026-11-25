package br.com.mutua.createchhub.space;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.mutua.createchhub.support.PostgresTestConfiguration;
import br.com.mutua.createchhub.support.SessionClient;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(PostgresTestConfiguration.class)
class ReservationApiTests {
  private static final String PATH = "/api/v1/reservations";
  private static final String CANCEL_PATH = "/api/v1/reservations/cancel";
  private final JsonMapper json = JsonMapper.builder().build();
  @LocalServerPort private int port;
  @Autowired private SpaceRepository spaces;
  @Autowired private SpaceService spaceService;
  @Autowired private StationLayoutService layout;
  @Autowired private JdbcTemplate jdbc;

  @AfterEach
  void cleanup() {
    jdbc.update("DELETE FROM reservations");
    spaces.deleteAllInBatch();
  }

  @Test
  void onlyOneConcurrentRequestCanOccupyAStationAndReceiptUsesItsPrivateToken() throws Exception {
    var room = spaces.saveAndFlush(new Space("Coworking", SpaceType.ROOM, 2, null));
    spaceService.changeBookingMode(room.getId(), BookingMode.PER_STATION);
    var stations = layout.generate(room.getId(), new StationBatchRequest(2, 2));
    var body = request(room, stations.getFirst());
    try (var first = new SessionClient(port);
        var second = new SessionClient(port);
        var executor = Executors.newVirtualThreadPerTaskExecutor()) {
      var firstCsrf = first.csrf();
      var secondCsrf = second.csrf();
      var gate = new CountDownLatch(1);
      var a =
          executor.submit(
              () -> {
                gate.await();
                return first.json("POST", PATH, body, firstCsrf);
              });
      var b =
          executor.submit(
              () -> {
                gate.await();
                return second.json("POST", PATH, body, secondCsrf);
              });
      gate.countDown();
      var results = List.of(a.get(15, TimeUnit.SECONDS), b.get(15, TimeUnit.SECONDS));
      assertThat(results.stream().map(HttpResponse::statusCode))
          .containsExactlyInAnyOrder(201, 409);
      var created =
          results.stream().filter(result -> result.statusCode() == 201).findFirst().orElseThrow();
      var result = json.readTree(created.body());
      var protocol = result.path("receipt").path("protocol").asString();
      var token = result.path("trackingToken").asString();
      assertThat(result.path("receipt").path("status").asString()).isEqualTo("PENDING");
      assertThat(result.path("receipt").path("cancellable").asBoolean()).isTrue();
      assertThat(result.path("receipt").path("lateCancellationWarning").asBoolean()).isFalse();
      assertThat(result.path("receipt").path("lateCancellation").asBoolean()).isFalse();
      assertThat(result.path("receipt").has("cancelledAt")).isFalse();
      assertThat(receipt(protocol, 400)).doesNotContain("Coworking");
      assertThat(receipt("a".repeat(43), 404)).doesNotContain("Coworking");
      assertThat(receipt(token, 200))
          .contains(protocol, "Coworking")
          .doesNotContain("52998224725", "visitor@example.com", token);
      assertThat(
              jdbc.queryForObject(
                  "SELECT count(*) FROM reservations WHERE rules_accepted_at IS NOT NULL",
                  Integer.class))
          .isEqualTo(1);
      var availability =
          json.readTree(
              first
                  .get(
                      "/api/v1/spaces/"
                          + room.getId()
                          + "/availability?startsAt=2099-03-10T09:00&endsAt=2099-03-10T10:00")
                  .body());
      assertThat(availability.path("stations").get(0).path("available").asBoolean()).isFalse();
      assertThat(availability.path("stations").get(1).path("available").asBoolean()).isTrue();
      var adjacent = new HashMap<>(body);
      adjacent.put("startsAt", "2099-03-10T10:00");
      adjacent.put("endsAt", "2099-03-10T11:00");
      assertThat(first.json("POST", PATH, adjacent, firstCsrf).statusCode()).isEqualTo(201);
    }
  }

  @Test
  void cancelsWithPrivateTokenReleasesAvailabilityAndPreservesIdempotencyUnderConcurrency()
      throws Exception {
    var room = spaces.saveAndFlush(new Space("Coworking", SpaceType.ROOM, 2, null));
    spaceService.changeBookingMode(room.getId(), BookingMode.PER_STATION);
    var station = layout.generate(room.getId(), new StationBatchRequest(1, 1)).getFirst();
    var body = request(room, station);
    try (var visitor = new SessionClient(port);
        var other = new SessionClient(port);
        var executor = Executors.newVirtualThreadPerTaskExecutor()) {
      var csrf = visitor.csrf();
      var created = json.readTree(visitor.json("POST", PATH, body, csrf).body());
      var protocol = created.path("receipt").path("protocol").asString();
      var token = created.path("trackingToken").asString();

      assertThat(
              visitor
                  .json(
                      "POST",
                      CANCEL_PATH,
                      Map.of("confirmLateCancellation", false),
                      null,
                      Map.of("X-Reservation-Token", token))
                  .statusCode())
          .isEqualTo(403);
      assertThat(
              visitor
                  .json(
                      "POST",
                      CANCEL_PATH,
                      Map.of("confirmLateCancellation", false),
                      csrf,
                      Map.of("X-Reservation-Token", protocol))
                  .statusCode())
          .isEqualTo(400);
      assertThat(
              visitor
                  .json(
                      "POST",
                      CANCEL_PATH,
                      Map.of("confirmLateCancellation", false),
                      csrf,
                      Map.of("X-Reservation-Token", "b".repeat(43)))
                  .statusCode())
          .isEqualTo(404);

      var otherCsrf = other.csrf();
      var gate = new CountDownLatch(1);
      var firstCancel =
          executor.submit(
              () -> {
                gate.await();
                return visitor.json(
                    "POST",
                    CANCEL_PATH,
                    Map.of("confirmLateCancellation", false),
                    csrf,
                    Map.of("X-Reservation-Token", token));
              });
      var secondCancel =
          executor.submit(
              () -> {
                gate.await();
                return other.json(
                    "POST",
                    CANCEL_PATH,
                    Map.of("confirmLateCancellation", true),
                    otherCsrf,
                    Map.of("X-Reservation-Token", token));
              });
      gate.countDown();

      var cancelA = firstCancel.get(15, TimeUnit.SECONDS);
      var cancelB = secondCancel.get(15, TimeUnit.SECONDS);
      assertThat(cancelA.statusCode()).isEqualTo(200);
      assertThat(cancelB.statusCode()).isEqualTo(200);
      var cancelledNode = json.readTree(cancelA.body());
      assertThat(json.readTree(cancelB.body())).isEqualTo(cancelledNode);
      assertThat(cancelledNode.path("status").asString()).isEqualTo("CANCELLED");
      assertThat(cancelledNode.path("cancellable").asBoolean()).isFalse();
      assertThat(cancelledNode.path("lateCancellationWarning").asBoolean()).isFalse();
      assertThat(cancelledNode.path("lateCancellation").asBoolean()).isFalse();
      assertThat(cancelledNode.path("cancelledAt").asString()).isNotBlank();
      assertThat(json.readTree(receipt(token, 200))).isEqualTo(cancelledNode);

      var availability =
          json.readTree(
              visitor
                  .get(
                      "/api/v1/spaces/"
                          + room.getId()
                          + "/availability?startsAt=2099-03-10T09:00&endsAt=2099-03-10T10:00")
                  .body());
      assertThat(availability.path("stations").get(0).path("available").asBoolean()).isTrue();
      assertThat(other.json("POST", PATH, body, otherCsrf).statusCode()).isEqualTo(201);
      assertThat(jdbc.queryForObject("SELECT count(*) FROM reservations", Integer.class))
          .isEqualTo(2);
    }
  }

  @Test
  void warnsAndMarksLateCancellationWithin24HoursAndRejectsEndedOrRejectedReservations()
      throws Exception {
    var room = spaces.saveAndFlush(new Space("Sala de reunião", SpaceType.ROOM, 6, null));
    var body = new HashMap<String, Object>();
    body.put("spaceId", room.getId());
    body.put("startsAt", "2099-03-10T09:00");
    body.put("endsAt", "2099-03-10T10:00");
    body.put("requester", requester("52998224725"));
    body.put("rulesAccepted", true);

    try (var visitor = new SessionClient(port)) {
      var csrf = visitor.csrf();
      var first = json.readTree(visitor.json("POST", PATH, body, csrf).body());
      var firstId = UUID.fromString(first.path("receipt").path("protocol").asString());
      var firstToken = first.path("trackingToken").asString();

      var nowHub =
          LocalDateTime.now(SpaceAvailabilityService.HUB_ZONE).truncatedTo(ChronoUnit.MINUTES);
      updateInterval(firstId, nowHub.plusHours(2), nowHub.plusHours(3));

      var receiptBefore = json.readTree(receipt(firstToken, 200));
      assertThat(receiptBefore.path("cancellable").asBoolean()).isTrue();
      assertThat(receiptBefore.path("lateCancellationWarning").asBoolean()).isTrue();

      var unconfirmedLate =
          visitor.json(
              "POST",
              CANCEL_PATH,
              Map.of("confirmLateCancellation", false),
              csrf,
              Map.of("X-Reservation-Token", firstToken));
      assertThat(unconfirmedLate.statusCode()).isEqualTo(409);
      assertThat(
              jdbc.queryForObject(
                  "SELECT status FROM reservations WHERE id = ?", String.class, firstId))
          .isEqualTo("PENDING");

      var confirmedLate =
          visitor.json(
              "POST",
              CANCEL_PATH,
              Map.of("confirmLateCancellation", true),
              csrf,
              Map.of("X-Reservation-Token", firstToken));
      assertThat(confirmedLate.statusCode()).isEqualTo(200);
      var lateNode = json.readTree(confirmedLate.body());
      assertThat(lateNode.path("status").asString()).isEqualTo("CANCELLED");
      assertThat(lateNode.path("lateCancellation").asBoolean()).isTrue();
      assertThat(lateNode.path("cancelledAt").asString()).isNotBlank();

      var second = json.readTree(visitor.json("POST", PATH, body, csrf).body());
      var secondId = UUID.fromString(second.path("receipt").path("protocol").asString());
      var secondToken = second.path("trackingToken").asString();
      updateInterval(secondId, nowHub.minusHours(2), nowHub.minusHours(1));
      assertThat(json.readTree(receipt(secondToken, 200)).path("cancellable").asBoolean())
          .isFalse();
      assertThat(
              visitor
                  .json(
                      "POST",
                      CANCEL_PATH,
                      Map.of("confirmLateCancellation", true),
                      csrf,
                      Map.of("X-Reservation-Token", secondToken))
                  .statusCode())
          .isEqualTo(409);

      var third = json.readTree(visitor.json("POST", PATH, body, csrf).body());
      var thirdId = UUID.fromString(third.path("receipt").path("protocol").asString());
      var thirdToken = third.path("trackingToken").asString();
      jdbc.update("UPDATE reservations SET status = 'REJECTED' WHERE id = ?", thirdId);
      assertThat(json.readTree(receipt(thirdToken, 200)).path("cancellable").asBoolean()).isFalse();
      assertThat(
              visitor
                  .json(
                      "POST",
                      CANCEL_PATH,
                      Map.of("confirmLateCancellation", true),
                      csrf,
                      Map.of("X-Reservation-Token", thirdToken))
                  .statusCode())
          .isEqualTo(409);
    }
  }

  @Test
  void rejectsMissingAcceptanceInvalidCpfAndMissingCsrf() throws Exception {
    var room = spaces.saveAndFlush(new Space("Coworking", SpaceType.ROOM, 2, null));
    spaceService.changeBookingMode(room.getId(), BookingMode.PER_STATION);
    var station = layout.generate(room.getId(), new StationBatchRequest(1, 1)).getFirst();
    var body = request(room, station);
    try (var visitor = new SessionClient(port)) {
      assertThat(visitor.json("POST", PATH, body, null).statusCode()).isEqualTo(403);
      var csrf = visitor.csrf();
      body.put("rulesAccepted", false);
      assertThat(visitor.json("POST", PATH, body, csrf).statusCode()).isEqualTo(400);
      body.put("rulesAccepted", true);
      body.put("requester", requester("11111111111"));
      assertThat(visitor.json("POST", PATH, body, csrf).statusCode()).isEqualTo(400);
      assertThat(jdbc.queryForObject("SELECT count(*) FROM reservations", Integer.class)).isZero();
    }
  }

  private void updateInterval(UUID id, LocalDateTime startsAt, LocalDateTime endsAt) {
    jdbc.update(
        "UPDATE reservations SET starts_at = ?, ends_at = ? WHERE id = ?",
        Timestamp.valueOf(startsAt),
        Timestamp.valueOf(endsAt),
        id);
  }

  private HashMap<String, Object> request(Space room, SpaceResponse station) {
    return new HashMap<>(
        Map.of(
            "spaceId",
            room.getId(),
            "stationId",
            station.id(),
            "startsAt",
            "2099-03-10T09:00",
            "endsAt",
            "2099-03-10T10:00",
            "requester",
            requester("52998224725"),
            "rulesAccepted",
            true));
  }

  private Map<String, Object> requester(String cpf) {
    return Map.of(
        "cpf",
        cpf,
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
        "Trabalho",
        "notes",
        "");
  }

  private String receipt(String token, int expected) throws Exception {
    try (var client = HttpClient.newHttpClient()) {
      var request =
          HttpRequest.newBuilder(URI.create("http://localhost:" + port + PATH + "/receipt"))
              .timeout(Duration.ofSeconds(5))
              .header("X-Reservation-Token", token)
              .GET()
              .build();
      var response = client.send(request, HttpResponse.BodyHandlers.ofString());
      assertThat(response.statusCode()).isEqualTo(expected);
      return response.body();
    }
  }
}
