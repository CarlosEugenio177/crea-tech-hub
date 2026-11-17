package br.com.mutua.createchhub.config;

import br.com.mutua.createchhub.auth.AdminSessionFilter;
import br.com.mutua.createchhub.auth.AdminSessionValidator;
import br.com.mutua.createchhub.auth.AdminUserDetailsService;
import br.com.mutua.createchhub.shared.error.SecurityProblemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.SecurityContextHolderFilter;

@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@EnableMethodSecurity
public class SecurityConfiguration {

  @Bean
  SecurityFilterChain securityFilterChain(
      HttpSecurity http,
      DaoAuthenticationProvider provider,
      SecurityProblemWriter problems,
      AdminSessionFilter sessions,
      @Value("${springdoc.api-docs.enabled:false}") boolean openApiEnabled)
      throws Exception {
    return http.authenticationProvider(provider)
        .addFilterAfter(sessions, SecurityContextHolderFilter.class)
        .authorizeHttpRequests(
            authorize -> {
              if (openApiEnabled) {
                authorize
                    .requestMatchers(
                        HttpMethod.GET,
                        "/v3/api-docs",
                        "/v3/api-docs/**",
                        "/v3/api-docs.yaml",
                        "/swagger-ui.html",
                        "/swagger-ui/**")
                    .permitAll();
              }
              authorize
                  .requestMatchers(
                      HttpMethod.GET,
                      "/actuator/health",
                      "/api/v1/admin/auth/csrf",
                      "/api/v1/hub/operating-hours",
                      "/api/v1/spaces/*/stations")
                  .permitAll()
                  .requestMatchers(HttpMethod.GET, "/api/v1/spaces")
                  .permitAll()
                  .requestMatchers(
                      HttpMethod.POST,
                      "/api/v1/admin/auth/login",
                      "/api/v1/admin/auth/password/forgot",
                      "/api/v1/admin/auth/password/reset")
                  .permitAll()
                  .requestMatchers("/api/v1/admin/**")
                  .hasRole("ADMIN")
                  .anyRequest()
                  .denyAll();
            })
        .exceptionHandling(
            exceptions ->
                exceptions
                    .authenticationEntryPoint(
                        (request, response, exception) ->
                            problems.write(
                                request,
                                response,
                                HttpStatus.UNAUTHORIZED,
                                "Autenticação necessária."))
                    .accessDeniedHandler(
                        (request, response, exception) ->
                            problems.write(
                                request, response, HttpStatus.FORBIDDEN, "Acesso não permitido.")))
        .requestCache(configurer -> configurer.disable())
        .formLogin(
            form ->
                form.loginPage("/api/v1/admin/auth/login")
                    .loginProcessingUrl("/api/v1/admin/auth/login")
                    .usernameParameter("email")
                    .successHandler((request, response, authentication) -> response.setStatus(204))
                    .failureHandler(
                        (request, response, exception) ->
                            problems.write(
                                request,
                                response,
                                HttpStatus.UNAUTHORIZED,
                                "E-mail ou senha inválidos.")))
        .httpBasic(configurer -> configurer.disable())
        .logout(
            logout ->
                logout
                    .logoutUrl("/api/v1/admin/auth/logout")
                    .deleteCookies("JSESSIONID")
                    .logoutSuccessHandler(
                        (request, response, authentication) -> response.setStatus(204)))
        .build();
  }

  @Bean
  AdminSessionFilter adminSessionFilter(
      AdminSessionValidator sessions, SecurityProblemWriter problems) {
    return new AdminSessionFilter(sessions, problems);
  }

  @Bean
  FilterRegistrationBean<AdminSessionFilter> adminSessionFilterRegistration(
      AdminSessionFilter filter) {
    var registration = new FilterRegistrationBean<>(filter);
    registration.setEnabled(false);
    return registration;
  }

  @Bean
  DaoAuthenticationProvider adminAuthenticationProvider(
      AdminUserDetailsService users, PasswordEncoder passwords) {
    var provider = new DaoAuthenticationProvider(users);
    provider.setPasswordEncoder(passwords);
    return provider;
  }
}
