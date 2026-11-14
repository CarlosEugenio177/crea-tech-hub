package br.com.mutua.createchhub.config;

import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder.SecretKeyFactoryAlgorithm;

@Configuration(proxyBeanMethods = false)
public class PasswordConfiguration {

	@Bean
	PasswordEncoder passwordEncoder() {
		var pbkdf2 = new Pbkdf2PasswordEncoder("", 16, 600_000,
				SecretKeyFactoryAlgorithm.PBKDF2WithHmacSHA256);
		var encoder = new DelegatingPasswordEncoder("pbkdf2-v1",
				Map.of("pbkdf2-v1", pbkdf2, "bcrypt", new BCryptPasswordEncoder()));
		encoder.setDefaultPasswordEncoderForMatches(new BCryptPasswordEncoder());
		return encoder;
	}
}
