package br.com.mutua.createchhub.openapi;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.mutua.createchhub.support.PostgresTestConfiguration;
import br.com.mutua.createchhub.support.SessionClient;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"springdoc.api-docs.enabled=true", "springdoc.swagger-ui.enabled=true"})
@Import(PostgresTestConfiguration.class)
class OpenApiContractTests {

  @LocalServerPort private int port;
  @Autowired private JsonMapper json;

  @Test
  void publishesTheActualApiAndKeepsTheVersionedContractSynchronized() throws Exception {
    try (var client = new SessionClient(port)) {
      var response = client.get("/v3/api-docs");
      assertThat(response.statusCode()).isEqualTo(200);
      var contract = json.readTree(response.body());
      assertThat(contract.path("openapi").asString()).startsWith("3.1.");
      assertThat(contract.path("paths").has("/api/v1/admin/team")).isTrue();
      assertThat(contract.path("paths").has("/api/v1/admin/spaces")).isTrue();
      assertThat(contract.path("paths").has("/api/v1/admin/auth/login")).isTrue();
      assertThat(contract.path("paths").has("/api/v1/admin/auth/logout")).isTrue();
      assertThat(contract.path("components").path("schemas").has("UserAccount")).isFalse();
      var paths = contract.path("paths");
      var login = paths.path("/api/v1/admin/auth/login").path("post");
      assertThat(login.path("responses").has("204")).isTrue();
      assertThat(login.path("requestBody").path("content").has("application/x-www-form-urlencoded"))
          .isTrue();
      assertThat(paths.path("/api/v1/admin/spaces").path("post").path("responses").has("201"))
          .isTrue();
      assertThat(paths.path("/api/v1/admin/team").path("post").path("responses").has("201"))
          .isTrue();
      assertThat(
              paths
                  .path("/api/v1/admin/auth/password/forgot")
                  .path("post")
                  .path("responses")
                  .has("202"))
          .isTrue();
      var schemes = contract.path("components").path("securitySchemes");
      assertThat(schemes.path("sessionAuth").path("in").asString()).isEqualTo("cookie");
      assertThat(schemes.path("sessionAuth").path("name").asString()).isEqualTo("JSESSIONID");
      assertThat(schemes.path("csrf").path("name").asString())
          .isEqualTo(client.csrf().path("headerName").asString());
      assertThat(paths.path("/api/v1/admin/auth/csrf").path("get").path("security").isEmpty())
          .isTrue();
      assertThat(login.path("security").path(0).has("csrf")).isTrue();
      assertThat(login.path("security").path(0).has("sessionAuth")).isFalse();
      var spaceWrite = paths.path("/api/v1/admin/spaces").path("post");
      assertThat(spaceWrite.path("security").path(0).has("sessionAuth")).isTrue();
      assertThat(spaceWrite.path("security").path(0).has("csrf")).isTrue();
      var schemas = contract.path("components").path("schemas");
      assertThat(schemas.path("SpaceRequest").path("properties").has("capacityCompatible"))
          .isFalse();
      assertThat(
              schemas.path("Space").path("properties").path("description").path("type").toString())
          .contains("null");
      assertThat(schemas.has("ProblemDetail")).isTrue();
      var output = Path.of("target", "openapi", "admin-api.json");
      Files.createDirectories(output.getParent());
      var formatted = json.writerWithDefaultPrettyPrinter().writeValueAsString(contract) + "\n";
      Files.writeString(output, formatted);
      var versioned = Path.of("..", "openapi", "admin-api.json");
      if (Boolean.getBoolean("openapi.update")) {
        Files.createDirectories(versioned.getParent());
        Files.writeString(versioned, formatted);
      }
      assertThat(Files.exists(versioned)).as("Gere o contrato com -Dopenapi.update=true").isTrue();
      assertThat(json.readTree(Files.readString(versioned))).isEqualTo(contract);
      assertThat(client.get("/v3/api-docs.yaml").statusCode()).isEqualTo(200);
      assertThat(client.get("/swagger-ui/index.html").statusCode()).isEqualTo(200);
      assertThat(client.get("/api/v1/admin/spaces").statusCode()).isEqualTo(401);
    }
  }
}
