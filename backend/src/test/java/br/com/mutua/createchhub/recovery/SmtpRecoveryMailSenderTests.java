package br.com.mutua.createchhub.recovery;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import br.com.mutua.createchhub.shared.error.ServiceUnavailableException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

class SmtpRecoveryMailSenderTests {

  @Test
  void sendsTheLinkOnlyToTheManagedAddressAndKeepsTheTokenOutOfTheQuery() {
    var transport = mock(JavaMailSender.class);
    var factory = new StaticListableBeanFactory();
    factory.addBean("mail", transport);
    var sender =
        new SmtpRecoveryMailSender(
            factory.getBeanProvider(JavaMailSender.class),
            "https://hub.example.com/redefinir-senha",
            "hub@example.com");
    sender.send("admin@example.com", "private-test-token");
    var message = ArgumentCaptor.forClass(SimpleMailMessage.class);
    verify(transport).send(message.capture());
    assertThat(message.getValue().getTo()).containsExactly("admin@example.com");
    assertThat(message.getValue().getText())
        .contains("#token=private-test-token")
        .doesNotContain("?token=");
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "",
        "http://hub.example.com/reset",
        "https://user:secret@hub.example.com/reset",
        "https://hub.example.com/reset?next=evil",
        "https://hub.example.com/reset#token=old",
        "invalid url"
      })
  void rejectsUnsafeOrMissingResetUrls(String url) {
    var factory = new StaticListableBeanFactory();
    factory.addBean("mail", mock(JavaMailSender.class));
    var sender =
        new SmtpRecoveryMailSender(
            factory.getBeanProvider(JavaMailSender.class), url, "hub@example.com");
    assertThatThrownBy(sender::requireConfigured).isInstanceOf(ServiceUnavailableException.class);
  }

  @Test
  void reportsMissingSmtpTransportAndPermitsHttpOnlyForLocalDevelopment() {
    var factory = new StaticListableBeanFactory();
    var sender =
        new SmtpRecoveryMailSender(
            factory.getBeanProvider(JavaMailSender.class),
            "http://localhost:3000/reset",
            "hub@example.com");
    assertThatThrownBy(sender::requireConfigured).isInstanceOf(ServiceUnavailableException.class);
    factory.addBean("mail", mock(JavaMailSender.class));
    sender.requireConfigured();
  }
}
