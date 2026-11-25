package br.com.mutua.createchhub.reservation;

import java.net.URI;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

final class ResendReservationSender {
  private static final DateTimeFormatter DATE_TIME =
      DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");
  private final RestClient client;
  private final String apiKey;
  private final String from;

  ResendReservationSender(RestClient client, ReservationEmailSettings settings) {
    this.client = client;
    apiKey = required(settings.apiKey(), "RESERVATION_EMAIL_RESEND_API_KEY");
    from = required(settings.from(), "RESERVATION_EMAIL_FROM");
  }

  Result send(ReservationEmailQueue.Message message, URI link) {
    String title =
        switch (message.status()) {
          case PENDING -> "Solicitação de reserva recebida";
          case CONFIRMED -> "Reserva confirmada";
          case REJECTED -> "Solicitação de reserva recusada";
          case CANCELLED -> "Reserva cancelada";
        };
    var space = message.spaceName();
    if (message.stationNumber() != null) space += " · Estação " + message.stationNumber();
    var text =
        String.join(
            "\n",
            title,
            "",
            "Protocolo: " + message.reservationId(),
            "Ambiente: " + space,
            "Início: " + DATE_TIME.format(message.startsAt()),
            "Fim: " + DATE_TIME.format(message.endsAt()),
            "",
            "Veja a situação atual, a mensagem da equipe e as opções de cancelamento no seu link individual:",
            link.toString());
    try {
      client
          .post()
          .uri("/emails")
          .contentType(MediaType.APPLICATION_JSON)
          .headers(headers -> headers.setBearerAuth(apiKey))
          .header(
              "Idempotency-Key",
              "reservation/" + message.reservationId() + "/" + message.status().name())
          .body(new Email(from, List.of(message.recipient()), title, text))
          .retrieve()
          .toBodilessEntity();
      return Result.ACCEPTED;
    } catch (RestClientResponseException exception) {
      int status = exception.getStatusCode().value();
      return status == 429 || status == 409 || status >= 500 ? Result.RETRY : Result.FAILED;
    } catch (RestClientException exception) {
      return Result.RETRY;
    }
  }

  private static String required(String value, String variable) {
    if (value == null || value.isBlank() || value.indexOf('\r') >= 0 || value.indexOf('\n') >= 0) {
      throw new IllegalArgumentException("Configure " + variable + " para ativar os e-mails.");
    }
    return value.strip();
  }

  enum Result {
    ACCEPTED,
    RETRY,
    FAILED
  }

  private record Email(String from, List<String> to, String subject, String text) {}
}
