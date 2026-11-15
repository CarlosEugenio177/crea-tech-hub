package br.com.mutua.createchhub.recovery;

import br.com.mutua.createchhub.shared.error.ServiceUnavailableException;
import java.net.URI;
import java.util.Set;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class SmtpRecoveryMailSender implements RecoveryMailSender {

  private final ObjectProvider<JavaMailSender> sender;
  private final String resetUrl;
  private final String from;

  public SmtpRecoveryMailSender(
      ObjectProvider<JavaMailSender> sender,
      @Value("${app.recovery.url:}") String resetUrl,
      @Value("${app.recovery.from:}") String from) {
    this.sender = sender;
    this.resetUrl = resetUrl;
    this.from = from;
  }

  @Override
  public void requireConfigured() {
    if (sender.getIfAvailable() == null || from.isBlank() || !validUrl()) {
      throw new ServiceUnavailableException(
          "Recuperação por e-mail indisponível. Contate o responsável pelo sistema.");
    }
  }

  private boolean validUrl() {
    try {
      var uri = URI.create(resetUrl);
      return uri.getHost() != null
          && uri.getRawUserInfo() == null
          && uri.getRawFragment() == null
          && uri.getRawQuery() == null
          && ("https".equals(uri.getScheme())
              || ("http".equals(uri.getScheme())
                  && Set.of("localhost", "127.0.0.1", "[::1]").contains(uri.getHost())));
    } catch (IllegalArgumentException exception) {
      return false;
    }
  }

  @Override
  public void send(String email, String token) {
    requireConfigured();
    var message = new SimpleMailMessage();
    message.setFrom(from);
    message.setTo(email);
    message.setSubject("Crea Tech Hub — recuperação de acesso");
    message.setText(
        "Para definir uma nova senha, acesse o link abaixo. Ele vale por 15 minutos e pode ser usado uma vez.\n\n"
            + resetUrl
            + "#token="
            + token
            + "\n\nSe você não solicitou a alteração, ignore esta mensagem.");
    sender.getObject().send(message);
  }
}
