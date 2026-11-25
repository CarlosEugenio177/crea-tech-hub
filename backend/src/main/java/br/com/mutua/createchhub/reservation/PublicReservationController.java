package br.com.mutua.createchhub.reservation;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PublicReservationController {
  private final ReservationService reservations;

  public PublicReservationController(ReservationService reservations) {
    this.reservations = reservations;
  }

  @Operation(operationId = "requestReservation", summary = "Solicitar reserva para análise")
  @PostMapping("/api/v1/reservations")
  @ResponseStatus(HttpStatus.CREATED)
  public ResponseEntity<ReservationService.ReservationSubmission> submit(
      @Valid @RequestBody ReservationRequest body) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .cacheControl(CacheControl.noStore())
        .body(reservations.submit(body));
  }

  @Operation(
      operationId = "getReservationReceipt",
      summary = "Consultar pedido pelo código privado")
  @GetMapping("/api/v1/reservations/receipt")
  public ResponseEntity<ReservationReceipt> receipt(
      @RequestHeader(name = "X-Reservation-Token") @Pattern(regexp = "[A-Za-z0-9_-]{43}")
          String token) {
    return ResponseEntity.ok()
        .cacheControl(CacheControl.noStore())
        .body(reservations.receipt(token));
  }

  @Operation(operationId = "cancelReservation", summary = "Cancelar reserva pelo código privado")
  @PostMapping("/api/v1/reservations/cancel")
  public ResponseEntity<ReservationReceipt> cancel(
      @RequestHeader(name = "X-Reservation-Token") @Pattern(regexp = "[A-Za-z0-9_-]{43}")
          String token,
      @Valid @RequestBody ReservationCancellationRequest body) {
    return ResponseEntity.ok()
        .cacheControl(CacheControl.noStore())
        .body(reservations.cancel(token, body));
  }
}
