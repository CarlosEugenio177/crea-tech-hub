package br.com.mutua.createchhub.operatinghours;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class UsageRulesResponseTests {

  @Test
  void returnsAcceptanceRequiredWithGeneralRules() {
    var response = UsageRulesResponse.current();

    assertThat(response.acceptanceRequired()).isTrue();
    assertThat(response.generalRules()).hasSize(4);
  }

  @Test
  void preservesWhenAcceptanceIsNotRequired() {
    var response = new UsageRulesResponse(false, List.of("Primeira regra"));

    assertThat(response.acceptanceRequired()).isFalse();
  }

  @Test
  void copiesTheProvidedRules() {
    var rules = new ArrayList<>(List.of("Primeira regra"));

    var response = new UsageRulesResponse(true, rules);
    rules.clear();

    assertThat(response.generalRules()).containsExactly("Primeira regra");
  }
}
