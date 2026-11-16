package br.com.mutua.createchhub.openapi;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "springdoc.api-docs.enabled", havingValue = "true")
public class OpenApiConfiguration {

  @Bean
  OpenAPI adminOpenApi() {
    return new OpenAPI()
        .info(
            new Info()
                .title("Crea Tech Hub — API administrativa")
                .version("1.0.0")
                .description(
                    "Sessão por cookie. Obtenha CSRF antes das escritas e renove o token após o login. Permissões são verificadas no backend."))
        .servers(List.of(new Server().url("/")))
        .components(
            new Components()
                .addSecuritySchemes(
                    "sessionAuth",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.COOKIE)
                        .name("JSESSIONID"))
                .addSecuritySchemes(
                    "csrf",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.HEADER)
                        .name("X-CSRF-TOKEN")));
  }
}
