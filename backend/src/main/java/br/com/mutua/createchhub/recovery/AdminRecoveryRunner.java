package br.com.mutua.createchhub.recovery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@Profile("recover-admin")
public class AdminRecoveryRunner implements ApplicationRunner {

  private static final Logger log = LoggerFactory.getLogger(AdminRecoveryRunner.class);
  private final AdminRecoveryService recovery;
  private final Environment environment;

  public AdminRecoveryRunner(AdminRecoveryService recovery, Environment environment) {
    this.recovery = recovery;
    this.environment = environment;
  }

  @Override
  public void run(ApplicationArguments arguments) {
    recovery.recover(
        environment.getRequiredProperty("RECOVERY_ADMIN_EMAIL"),
        environment.getRequiredProperty("RECOVERY_ADMIN_PASSWORD"));
    log.info(
        "Recuperação administrativa concluída. Remova o perfil e as variáveis de recuperação.");
  }
}
