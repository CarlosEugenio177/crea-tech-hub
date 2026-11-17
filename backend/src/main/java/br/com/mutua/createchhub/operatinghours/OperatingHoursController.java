package br.com.mutua.createchhub.operatinghours;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/hub/operating-hours")
@PreAuthorize("hasAuthority('MANAGE_SPACES')")
public class OperatingHoursController {

  private final OperatingHoursService hours;

  public OperatingHoursController(OperatingHoursService hours) {
    this.hours = hours;
  }

  @Operation(operationId = "getAdminOperatingHours", summary = "Consultar funcionamento do Hub")
  @GetMapping
  public OperatingHoursResponse current() {
    return hours.current();
  }

  @Operation(
      operationId = "updateOperatingHours",
      summary = "Atualizar dias e horários de funcionamento do Hub")
  @PutMapping
  public OperatingHoursResponse update(@Valid @RequestBody OperatingHoursRequest request) {
    return hours.update(request);
  }
}
