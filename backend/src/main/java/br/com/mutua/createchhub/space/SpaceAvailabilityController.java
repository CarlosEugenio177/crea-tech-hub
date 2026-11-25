package br.com.mutua.createchhub.space;

import br.com.mutua.createchhub.block.SpaceBlockRequest;
import br.com.mutua.createchhub.shared.error.InvalidRequestException;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.UUID;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SpaceAvailabilityController {
  private final SpaceAvailabilityService availability;

  public SpaceAvailabilityController(SpaceAvailabilityService availability) {
    this.availability = availability;
  }

  @Operation(operationId = "getSpaceDayAgenda", summary = "Consultar a agenda pública de um dia")
  @GetMapping("/api/v1/spaces/{id}/availability/day")
  public ResponseEntity<SpaceDayAgenda> day(
      @PathVariable UUID id,
      @RequestParam @Pattern(regexp = "^(?!0000)[0-9]{4}-[0-9]{2}-[0-9]{2}$") String date) {
    try {
      return ResponseEntity.ok()
          .cacheControl(CacheControl.noStore())
          .body(availability.day(id, LocalDate.parse(date)));
    } catch (DateTimeParseException exception) {
      throw new InvalidRequestException("Informe uma data válida.");
    }
  }

  @Operation(
      operationId = "getSpaceAvailability",
      summary = "Consultar disponibilidade no intervalo local do Hub")
  @GetMapping("/api/v1/spaces/{id}/availability")
  public ResponseEntity<SpaceAvailability> get(
      @PathVariable UUID id,
      @RequestParam @Pattern(regexp = SpaceBlockRequest.TIMESTAMP_PATTERN) String startsAt,
      @RequestParam @Pattern(regexp = SpaceBlockRequest.TIMESTAMP_PATTERN) String endsAt) {
    return ResponseEntity.ok()
        .cacheControl(CacheControl.noStore())
        .body(availability.check(id, startsAt, endsAt));
  }
}
