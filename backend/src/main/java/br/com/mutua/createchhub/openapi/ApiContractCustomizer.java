package br.com.mutua.createchhub.openapi;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import java.util.List;
import java.util.Set;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "springdoc.api-docs.enabled", havingValue = "true")
public class ApiContractCustomizer implements OpenApiCustomizer {

  private static final Set<String> PUBLIC_PATHS =
      Set.of(
          "/api/v1/admin/auth/csrf",
          "/api/v1/admin/auth/login",
          "/api/v1/admin/auth/password/forgot",
          "/api/v1/admin/auth/password/reset",
          "/api/v1/hub/operating-hours");

  @Override
  public void customise(OpenAPI api) {
    AuthenticationOpenApi.addFilterEndpoints(api);
    addProblemSchemas(api);
    api.getComponents()
        .addResponses(
            "Problem",
            new ApiResponse()
                .description("Erro no formato ProblemDetail.")
                .content(
                    new Content()
                        .addMediaType(
                            "application/problem+json",
                            new MediaType()
                                .schema(
                                    new ObjectSchema()
                                        .$ref("#/components/schemas/ProblemDetail")))));
    api.getPaths()
        .forEach(
            (path, item) ->
                item.readOperationsMap()
                    .forEach(
                        (method, operation) -> {
                          var requirement = new SecurityRequirement();
                          if (!PUBLIC_PATHS.contains(path)) requirement.addList("sessionAuth");
                          if (method != PathItem.HttpMethod.GET
                              && method != PathItem.HttpMethod.HEAD
                              && method != PathItem.HttpMethod.OPTIONS) {
                            requirement.addList("csrf");
                          }
                          operation.setSecurity(
                              requirement.isEmpty() ? List.of() : List.of(requirement));
                          operation
                              .getResponses()
                              .addApiResponse(
                                  "default",
                                  new ApiResponse().$ref("#/components/responses/Problem"));
                        }));
  }

  private void addProblemSchemas(OpenAPI api) {
    var validationError =
        new ObjectSchema()
            .addProperty("field", new StringSchema())
            .addProperty("message", new StringSchema());
    validationError.setRequired(List.of("field", "message"));
    var problem =
        new ObjectSchema()
            .addProperty("type", new StringSchema())
            .addProperty("title", new StringSchema())
            .addProperty("status", new IntegerSchema())
            .addProperty("detail", new StringSchema())
            .addProperty("instance", new StringSchema())
            .addProperty(
                "errors",
                new ArraySchema()
                    .items(new ObjectSchema().$ref("#/components/schemas/FieldValidationError")));
    api.getComponents()
        .addSchemas("ProblemDetail", problem)
        .addSchemas("FieldValidationError", validationError);
  }
}
