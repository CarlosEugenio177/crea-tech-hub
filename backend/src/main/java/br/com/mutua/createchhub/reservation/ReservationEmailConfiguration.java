package br.com.mutua.createchhub.reservation;

import java.net.http.HttpClient;
import java.time.Duration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestClient;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(ReservationEmailSettings.class)
class ReservationEmailConfiguration {
  @Configuration(proxyBeanMethods = false)
  @EnableScheduling
  @ConditionalOnProperty(prefix = "app.reservation-email", name = "enabled", havingValue = "true")
  static class Delivery {
    @Bean
    ResendReservationSender resendReservationSender(ReservationEmailSettings settings) {
      var http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
      var requests = new JdkClientHttpRequestFactory(http);
      requests.setReadTimeout(Duration.ofSeconds(10));
      return new ResendReservationSender(
          RestClient.builder().baseUrl("https://api.resend.com").requestFactory(requests).build(),
          settings);
    }

    @Bean
    ReservationEmailWorker reservationEmailWorker(
        ReservationEmailQueue queue,
        ResendReservationSender sender,
        ReservationEmailSettings settings) {
      return new ReservationEmailWorker(queue, sender, new ReservationEmailLinks(settings));
    }
  }
}
