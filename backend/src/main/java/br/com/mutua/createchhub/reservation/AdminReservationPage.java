package br.com.mutua.createchhub.reservation;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Schema(requiredProperties = {"items", "page", "size", "totalElements", "totalPages"})
public record AdminReservationPage(
    List<Entry> items, int page, int size, long totalElements, int totalPages) {
  public AdminReservationPage {
    items = List.copyOf(items);
  }

  @Schema(
      name = "AdminReservationEntry",
      requiredProperties = {
        "protocol",
        "spaceName",
        "startsAt",
        "endsAt",
        "status",
        "fullName",
        "email",
        "whatsapp",
        "reason",
        "notes",
        "lateCancellation"
      })
  public record Entry(
      UUID protocol,
      String spaceName,
      Integer stationNumber,
      LocalDateTime startsAt,
      LocalDateTime endsAt,
      ReservationStatus status,
      String fullName,
      String email,
      String whatsapp,
      String reason,
      String notes,
      boolean lateCancellation) {}
}
