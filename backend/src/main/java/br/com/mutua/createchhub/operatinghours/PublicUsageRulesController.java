package br.com.mutua.createchhub.operatinghours;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/hub/usage-rules")
public class PublicUsageRulesController {

  @Operation(
      operationId = "getUsageRules",
      summary = "Consultar regras gerais de uso do Hub",
      security = {})
  @GetMapping
  public UsageRulesResponse current() {
    return UsageRulesResponse.current();
  }
}
