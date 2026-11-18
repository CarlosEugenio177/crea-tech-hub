package br.com.mutua.createchhub.reservation;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(requiredProperties = {"protocol", "spaceName", "startsAt", "endsAt", "status"})
public record ReservationReceipt(
    UUID protocol,
    String spaceName,
    Integer stationNumber,
    LocalDateTime startsAt,
    LocalDateTime endsAt,
    String status) {}
