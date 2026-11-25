package br.com.mutua.createchhub.reservation;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.mutua.createchhub.space.Space;
import br.com.mutua.createchhub.space.SpaceRepository;
import br.com.mutua.createchhub.space.SpaceType;
import br.com.mutua.createchhub.support.PostgresTestConfiguration;
import br.com.mutua.createchhub.support.SessionClient;
import br.com.mutua.createchhub.user.AccessRole;
import br.com.mutua.createchhub.user.AdminPermission;
import br.com.mutua.createchhub.user.UserAccount;
import br.com.mutua.createchhub.user.UserAccountRepository;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(PostgresTestConfiguration.class)
class AdminReservationApiTests {
  private static final String PATH = "/api/v1/admin/reservations";
  private static final String PASSWORD = "Local-reservation-queue-test";
  private final JsonMapper json = JsonMapper.builder().build();
  @LocalServerPort private int port;
  @Autowired private SpaceRepository spaces;
  @Autowired private UserAccountRepository accounts;
  @Autowired private PasswordEncoder passwords;
  @Autowired private JdbcTemplate jdbc;

  @AfterEach
  void cleanup() {
    jdbc.update("DELETE FROM reservations");
    spaces.deleteAllInBatch();
    accounts.deleteAll();
  }

  @Test
  void filtersAndPaginatesReservationsWithoutExposingCpfOrTrackingHash() throws Exception {
    var space = spaces.saveAndFlush(new Space("Sala de estudo", SpaceType.ROOM, 2, null));
    var cancelled = row(space.getId(), "Mariana Lopes", "2099-03-10T09:00", "CANCELLED");
    row(space.getId(), "Mariana Santos", "2099-03-11T09:00", "PENDING");
    try (var client = signedIn("spaces@example.com", AdminPermission.MANAGE_SPACES)) {
      var list = json.readTree(client.get(PATH + "?size=1").body());
      assertThat(list.path("totalElements").asInt()).isEqualTo(2);
      assertThat(list.path("totalPages").asInt()).isEqualTo(2);
      assertThat(list.path("items").get(0).path("fullName").asString()).isEqualTo("Mariana Santos");
      var response =
          client.get(PATH + "?search=mari&space=estudo&date=2099-03-10&status=CANCELLED");
      assertThat(response.statusCode()).isEqualTo(200);
      assertThat(response.headers().firstValue("Cache-Control")).contains("no-store");
      var body = json.readTree(response.body());
      assertThat(body.path("totalElements").asInt()).isEqualTo(1);
      var entry = body.path("items").get(0);
      assertThat(entry.path("protocol").asString()).isEqualTo(cancelled.toString());
      assertThat(entry.path("lateCancellation").asBoolean()).isTrue();
      assertThat(entry.path("whatsapp").asString()).isEqualTo("86999990000");
      assertThat(entry.path("reason").asString()).isEqualTo("Estudo");
      assertThat(entry.has("cpf")).isFalse();
      assertThat(entry.has("trackingHash")).isFalse();
      assertThat(client.get(PATH + "?date=2099-02-31").statusCode()).isEqualTo(400);
    }
  }

  @Test
  void preventsAccessWithoutSessionOrSpacePermission() throws Exception {
    try (var visitor = new SessionClient(port);
        var member = signedIn("access@example.com", AdminPermission.MANAGE_ACCESS)) {
      assertThat(visitor.get(PATH).statusCode()).isEqualTo(401);
      assertThat(member.get(PATH).statusCode()).isEqualTo(403);
    }
  }

  private SessionClient signedIn(String email, AdminPermission permission) throws Exception {
    var account =
        new UserAccount("Operador de teste", email, passwords.encode(PASSWORD), AccessRole.ADMIN);
    account.replacePermissions(Set.of(permission));
    accounts.saveAndFlush(account);
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

  private UUID row(UUID spaceId, String name, String start, String status) {
    var id = UUID.randomUUID();
    var time = LocalDateTime.parse(start);
    boolean cancelled = status.equals("CANCELLED");
    jdbc.update(
        """
        INSERT INTO reservations (id, tracking_hash, space_id, space_name, starts_at, ends_at,
          cpf, full_name, email, whatsapp, profile, on_behalf_of_others, reason, notes, status,
          rules_accepted_at, cancelled_at, late_cancellation)
        VALUES (?, ?, ?, 'Sala de estudo', ?, ?, '52998224725', ?, 'visitor@example.com', '86999990000',
          'EXTERNAL', false, 'Estudo', '', ?, now(), CASE WHEN ? THEN now() END, ?)
        """,
        id,
        id.toString(),
        spaceId,
        time,
        time.plusHours(1),
        name,
        status,
        cancelled,
        cancelled);
    return id;
  }
}
