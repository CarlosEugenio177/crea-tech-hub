package br.com.mutua.createchhub.recovery;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

class RecoveryRequestLimiterTests {

  @Test
  void limitsByAddressAndReleasesTheWindowAfterExpiry() {
    var clock = mock(Clock.class);
    var now = Instant.parse("2026-11-15T15:00:00Z");
    when(clock.instant()).thenReturn(now);
    var limiter = new RecoveryRequestLimiter(clock);
    for (int i = 0; i < 10; i++) limiter.check("127.0.0.1");
    assertThatThrownBy(() -> limiter.check("127.0.0.1"))
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(
            exception ->
                org.assertj.core.api.Assertions.assertThat(
                        ((ResponseStatusException) exception).getStatusCode().value())
                    .isEqualTo(429));
    limiter.check("127.0.0.2");
    when(clock.instant()).thenReturn(now.plusSeconds(900));
    limiter.check("127.0.0.1");
  }
}
