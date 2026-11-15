package br.com.mutua.createchhub.recovery;

import java.time.Clock;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class RecoveryRequestLimiter {

  private final Clock clock;
  private final Map<String, Window> windows = new HashMap<>();

  public RecoveryRequestLimiter(Clock clock) {
    this.clock = clock;
  }

  public synchronized void check(String address) {
    var now = Instant.now(clock);
    windows.entrySet().removeIf(entry -> !entry.getValue().expiresAt().isAfter(now));
    var window = windows.get(address);
    if ((window == null && windows.size() >= 10000) || (window != null && window.count() >= 10)) {
      throw new ResponseStatusException(
          HttpStatus.TOO_MANY_REQUESTS, "Aguarde alguns minutos antes de tentar novamente.");
    }
    windows.put(
        address,
        window == null
            ? new Window(1, now.plusSeconds(900))
            : new Window(window.count() + 1, window.expiresAt()));
  }

  private record Window(int count, Instant expiresAt) {}
}
