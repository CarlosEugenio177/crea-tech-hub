package br.com.mutua.createchhub.openapi;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import java.util.List;

final class AuthenticationOpenApi {

  private AuthenticationOpenApi() {}

  static void addFilterEndpoints(OpenAPI api) {
    var login =
        new ObjectSchema()
            .addProperty("email", new StringSchema())
            .addProperty("password", new StringSchema().format("password").writeOnly(true));
    login.setRequired(List.of("email", "password"));
    api.getComponents().addSchemas("LoginRequest", login);
    api.path(
        "/api/v1/admin/auth/login",
        new PathItem()
            .post(
                new Operation()
                    .operationId("login")
                    .summary("Iniciar sessão administrativa")
                    .tags(List.of("Autenticação"))
                    .requestBody(
                        new RequestBody()
                            .required(true)
                            .content(
                                new Content()
                                    .addMediaType(
                                        "application/x-www-form-urlencoded",
                                        new MediaType()
                                            .schema(
                                                new ObjectSchema()
                                                    .$ref("#/components/schemas/LoginRequest")))))
                    .responses(
                        new ApiResponses()
                            .addApiResponse(
                                "204",
                                new ApiResponse()
                                    .description("Sessão iniciada; obtenha um novo CSRF.")))));
    api.path(
        "/api/v1/admin/auth/logout",
        new PathItem()
            .post(
                new Operation()
                    .operationId("logout")
                    .summary("Encerrar sessão")
                    .tags(List.of("Autenticação"))
                    .responses(
                        new ApiResponses()
                            .addApiResponse(
                                "204", new ApiResponse().description("Sessão encerrada.")))));
  }
}
