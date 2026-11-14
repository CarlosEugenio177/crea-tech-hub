package br.com.mutua.createchhub.auth;

import br.com.mutua.createchhub.user.AccessRole;
import br.com.mutua.createchhub.user.UserAccount;
import br.com.mutua.createchhub.user.UserAccountRepository;
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

	public AdminBootstrap(UserAccountRepository repository, PasswordEncoder passwords, Environment environment) {
		this.repository = repository;
		this.passwords = passwords;
		this.environment = environment;
	}

	@Override
	@Transactional
	public void run(ApplicationArguments arguments) {
		if (repository.existsByAccessRole(AccessRole.ADMIN)) {
			log.info("Inicialização administrativa ignorada: já existe administrador.");
			return;
		}
		var password = environment.getRequiredProperty("BOOTSTRAP_ADMIN_PASSWORD");
		if (password.length() < 12 || password.length() > 128) {
			throw new IllegalStateException("BOOTSTRAP_ADMIN_PASSWORD deve conter de 12 a 128 caracteres.");
		}
		repository.saveAndFlush(new UserAccount(
				environment.getRequiredProperty("BOOTSTRAP_ADMIN_NAME"),
				environment.getRequiredProperty("BOOTSTRAP_ADMIN_EMAIL"),
				passwords.encode(password), AccessRole.ADMIN));
		log.info("Conta administrativa inicial criada.");
	}
}
