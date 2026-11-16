package br.com.mutua.createchhub;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.mutua.createchhub.support.PostgresTestConfiguration;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(PostgresTestConfiguration.class)
class BackendFoundationTests {

  @LocalServerPort private int port;

  @Test
  void healthIsPublicAndDoesNotExposeInternalDetails() throws Exception {
    var response = send(request("/actuator/health").GET().build());

    assertThat(response.statusCode()).isEqualTo(200);
    var body = JsonMapper.builder().build().readTree(response.body());
    assertThat(body.path("status").asString()).isEqualTo("UP");
    assertThat(body.has("components")).isFalse();
    assertThat(body.has("details")).isFalse();
    assertThat(response.headers().firstValue("Content-Type"))
        .hasValueSatisfying(contentType -> assertThat(contentType).contains("json"));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "/api/v1/reservations",
        "/admin",
        "/actuator/env",
        "/actuator",
        "/login",
        "/v3/api-docs",
        "/v3/api-docs.yaml",
        "/swagger-ui/index.html",
        "/actuator/health/liveness",
        "/actuator/health/readiness"
      })
  void otherRoutesAreDeniedWithoutRedirectingToALoginPage(String path) throws Exception {
    var response = send(request(path).GET().build());

    assertThat(response.statusCode()).isEqualTo(401);
    assertThat(response.headers().firstValue("Location")).isEmpty();
  }

  @Test
  void healthDoesNotAllowWrites() throws Exception {
    var response =
        send(request("/actuator/health").POST(HttpRequest.BodyPublishers.noBody()).build());

    assertThat(response.statusCode()).isEqualTo(403);
  }

  private HttpRequest.Builder request(String path) {
    return HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
        .timeout(Duration.ofSeconds(5));
  }

  private HttpResponse<String> send(HttpRequest request) throws Exception {
    try (var client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build()) {
      return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
  }
}
