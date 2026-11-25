package br.com.mutua.createchhub.reservation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AdminReservationDecisionRequest(
    @NotNull ReservationStatus expectedStatus,
    @NotNull Action action,
    @Size(max = 1000) String note) {

  @Schema(name = "AdminReservationAction", enumAsRef = true)
  public enum Action {
    CONFIRM,
    REJECT,
    CANCEL
  }
}
