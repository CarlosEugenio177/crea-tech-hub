package br.com.mutua.createchhub.recovery;

import br.com.mutua.createchhub.shared.error.ServiceUnavailableException;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.stereotype.Service;

@Service
public class PasswordRecoveryService {

  private static final Logger log = LoggerFactory.getLogger(PasswordRecoveryService.class);
  private final PasswordResetService tokens;
  private final RecoveryMailSender mail;
  private final TaskExecutor executor;

  public PasswordRecoveryService(
      PasswordResetService tokens,
      RecoveryMailSender mail,
      @Qualifier("passwordRecoveryExecutor") TaskExecutor executor) {
    this.tokens = tokens;
    this.mail = mail;
    this.executor = executor;
  }

  public void request(String email) {
    mail.requireConfigured();
    var normalized = email.strip().toLowerCase(Locale.ROOT);
    try {
      executor.execute(() -> deliver(normalized));
    } catch (TaskRejectedException exception) {
      throw new ServiceUnavailableException(
          "Recuperação temporariamente indisponível. Tente novamente mais tarde.");
    }
  }

  private void deliver(String email) {
    try {
      tokens.issue(email).ifPresent(grant -> mail.send(grant.email(), grant.token()));
    } catch (Exception exception) {
      log.warn(
          "Falha na entrega de recuperação de acesso: {}", exception.getClass().getSimpleName());
    }
  }
}
