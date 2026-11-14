package br.com.mutua.createchhub.config;

import br.com.mutua.createchhub.auth.AdminUserDetailsService;
import br.com.mutua.createchhub.shared.error.SecurityProblemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration(proxyBeanMethods = false)
public class SecurityConfiguration {

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http, DaoAuthenticationProvider provider,
			SecurityProblemWriter problems) throws Exception {
		return http
				.authenticationProvider(provider)
				.authorizeHttpRequests(authorize -> authorize
						.requestMatchers(HttpMethod.GET, "/actuator/health", "/api/v1/admin/auth/csrf").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/v1/admin/auth/login").permitAll()
						.requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
						.anyRequest().denyAll())
				.exceptionHandling(exceptions -> exceptions
						.authenticationEntryPoint((request, response, exception) ->
								problems.write(request, response, HttpStatus.UNAUTHORIZED, "Autenticação necessária."))
						.accessDeniedHandler((request, response, exception) ->
								problems.write(request, response, HttpStatus.FORBIDDEN, "Acesso não permitido.")))
				.requestCache(AbstractHttpConfigurer::disable)
				.formLogin(form -> form
						.loginPage("/api/v1/admin/auth/login")
						.loginProcessingUrl("/api/v1/admin/auth/login")
						.usernameParameter("email")
						.successHandler((request, response, authentication) -> response.setStatus(204))
						.failureHandler((request, response, exception) ->
								problems.write(request, response, HttpStatus.UNAUTHORIZED, "E-mail ou senha inválidos.")))
				.httpBasic(AbstractHttpConfigurer::disable)
				.logout(logout -> logout
						.logoutUrl("/api/v1/admin/auth/logout")
						.deleteCookies("JSESSIONID")
						.logoutSuccessHandler((request, response, authentication) -> response.setStatus(204)))
				.build();
	}

	@Bean
	DaoAuthenticationProvider adminAuthenticationProvider(AdminUserDetailsService users, PasswordEncoder passwords) {
		var provider = new DaoAuthenticationProvider(users);
		provider.setPasswordEncoder(passwords);
		return provider;
	}
}
