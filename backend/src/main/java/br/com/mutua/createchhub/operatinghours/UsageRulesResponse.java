package br.com.mutua.createchhub.operatinghours;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(
    name = "UsageRules",
    requiredProperties = {"acceptanceRequired", "generalRules"})
public record UsageRulesResponse(boolean acceptanceRequired, List<String> generalRules) {

  private static final List<String> CURRENT_GENERAL_RULES =
      List.of(
          "Todas as solicitações passam por análise manual antes da confirmação.",
          "Solicitações pendentes ou confirmadas bloqueiam o intervalo reservado.",
          "O cancelamento pode ser realizado a qualquer momento.",
          "Cancelamentos com menos de 24 horas de antecedência são registrados como tardios.");

  public UsageRulesResponse {
    generalRules = List.copyOf(generalRules);
  }

  public static UsageRulesResponse current() {
    return new UsageRulesResponse(true, CURRENT_GENERAL_RULES);
  }
}
