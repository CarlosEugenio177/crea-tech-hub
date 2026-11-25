package br.com.mutua.createchhub.reservation;

import br.com.mutua.createchhub.auth.AdminPrincipal;
import br.com.mutua.createchhub.shared.error.InvalidRequestException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.UUID;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@PreAuthorize("hasAuthority('MANAGE_SPACES')")
public class AdminReservationController {
  private final AdminReservationQuery reservations;
  private final AdminReservationDecisionService decisions;

  public AdminReservationController(
      AdminReservationQuery reservations, AdminReservationDecisionService decisions) {
    this.reservations = reservations;
    this.decisions = decisions;
  }

  @PostMapping("/api/v1/admin/reservations/{id}/decisions")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(operationId = "decideAdminReservation", summary = "Registrar decisão sobre a reserva")
  @ApiResponse(responseCode = "404", ref = "#/components/responses/Problem")
  @ApiResponse(responseCode = "409", ref = "#/components/responses/Problem")
  public void decide(
      @PathVariable UUID id,
      @Valid @RequestBody AdminReservationDecisionRequest request,
      @AuthenticationPrincipal AdminPrincipal admin) {
    decisions.decide(id, admin.getAccountId(), request);
  }

  @GetMapping("/api/v1/admin/reservations")
  @Operation(operationId = "listAdminReservations", summary = "Consultar solicitações de reserva")
  public ResponseEntity<AdminReservationPage> list(
      @RequestParam(required = false) @Size(max = 150) String search,
      @RequestParam(required = false) @Size(max = 150) String space,
      @RequestParam(required = false) @Pattern(regexp = "(?!0000)[0-9]{4}-[0-9]{2}-[0-9]{2}")
          String date,
      @RequestParam(required = false) ReservationStatus status,
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
    var filters = new AdminReservationQuery.Filters(search, space, parseDate(date), status);
    return ResponseEntity.ok()
        .cacheControl(CacheControl.noStore())
        .body(reservations.list(filters, page, size));
  }

  private LocalDate parseDate(String date) {
    if (date == null) return null;
    try {
      return LocalDate.parse(date);
    } catch (DateTimeParseException exception) {
      throw new InvalidRequestException("Informe uma data válida.");
    }
  }
}
