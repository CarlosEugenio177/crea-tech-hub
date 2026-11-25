package br.com.mutua.createchhub.reservation;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(
    requiredProperties = {
      "protocol",
      "spaceName",
      "startsAt",
      "endsAt",
      "status",
      "cancellable",
      "lateCancellationWarning",
      "lateCancellation"
    })
public record ReservationReceipt(
    UUID protocol,
    String spaceName,
    Integer stationNumber,
    LocalDateTime startsAt,
    LocalDateTime endsAt,
    ReservationStatus status,
    boolean cancellable,
    boolean lateCancellationWarning,
    boolean lateCancellation,
    Instant cancelledAt) {}
