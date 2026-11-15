package br.com.mutua.createchhub.auth;

import br.com.mutua.createchhub.access.AdminAccessGuard;
import br.com.mutua.createchhub.user.AccessRole;
import br.com.mutua.createchhub.user.AdminPermission;
import br.com.mutua.createchhub.user.UserAccount;
import br.com.mutua.createchhub.user.UserAccountRepository;
import java.util.EnumSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("bootstrap-admin")
public class AdminBootstrap implements ApplicationRunner {

  private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);

  private final UserAccountRepository repository;
  private final PasswordEncoder passwords;
  private final Environment environment;
  private final AdminAccessGuard guard;

  public AdminBootstrap(
      UserAccountRepository repository,
      PasswordEncoder passwords,
      Environment environment,
      AdminAccessGuard guard) {
    this.repository = repository;
    this.passwords = passwords;
    this.environment = environment;
    this.guard = guard;
  }

  @Override
  @Transactional
  public void run(ApplicationArguments arguments) {
    guard.lock();
    if (repository.existsByAccessRole(AccessRole.ADMIN)) {
      log.info("Inicialização administrativa ignorada: já existe administrador.");
      return;
    }
    var password = environment.getRequiredProperty("BOOTSTRAP_ADMIN_PASSWORD");
    if (password.length() < 12 || password.length() > 128) {
      throw new IllegalStateException(
          "BOOTSTRAP_ADMIN_PASSWORD deve conter de 12 a 128 caracteres.");
    }
    var account =
        new UserAccount(
            environment.getRequiredProperty("BOOTSTRAP_ADMIN_NAME"),
            environment.getRequiredProperty("BOOTSTRAP_ADMIN_EMAIL"),
            passwords.encode(password),
            AccessRole.ADMIN);
    account.replacePermissions(EnumSet.allOf(AdminPermission.class));
    repository.saveAndFlush(account);
    log.info("Conta administrativa inicial criada.");
  }
}
