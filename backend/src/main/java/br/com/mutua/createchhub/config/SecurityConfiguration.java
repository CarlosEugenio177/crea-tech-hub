package br.com.mutua.createchhub.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

@Configuration(proxyBeanMethods = false)
public class SecurityConfiguration {

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http
				.authorizeHttpRequests(authorize -> authorize
						.requestMatchers(HttpMethod.GET, "/actuator/health").permitAll()
						.anyRequest().denyAll())
				.exceptionHandling(exceptions -> exceptions
						.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
						.accessDeniedHandler((request, response, exception) ->
								response.setStatus(HttpStatus.FORBIDDEN.value())))
				.requestCache(AbstractHttpConfigurer::disable)
				.formLogin(AbstractHttpConfigurer::disable)
				.httpBasic(AbstractHttpConfigurer::disable)
				.logout(AbstractHttpConfigurer::disable)
				.build();
	}
}
