package br.com.mutua.createchhub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;

@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class CreaTechHubApplication {

  public static void main(String[] args) {
    SpringApplication.run(CreaTechHubApplication.class, args);
  }
}
