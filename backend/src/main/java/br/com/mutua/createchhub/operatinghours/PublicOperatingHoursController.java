package br.com.mutua.createchhub.operatinghours;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/hub/operating-hours")
public class PublicOperatingHoursController {

  private final OperatingHoursService hours;

  public PublicOperatingHoursController(OperatingHoursService hours) {
    this.hours = hours;
  }

  @Operation(
      operationId = "getOperatingHours",
      summary = "Consultar funcionamento público do Hub",
      security = {})
  @GetMapping
  public OperatingHoursResponse current() {
    return hours.current();
  }
}
