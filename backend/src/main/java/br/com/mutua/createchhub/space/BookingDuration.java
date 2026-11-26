package br.com.mutua.createchhub.space;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record BookingDuration(
    @Positive(message = "A duração mínima deve ser maior que zero.")
        @Max(value = 1439, message = "A duração mínima deve ser de até 1439 minutos.")
        @Schema(
            description =
                "Duração mínima em minutos. Ausência representa nenhum mínimo configurado.",
            example = "30")
        Integer minMinutes,
    @Positive(message = "A duração máxima deve ser maior que zero.")
        @Max(value = 1439, message = "A duração máxima deve ser de até 1439 minutos.")
        @Schema(
            description =
                "Duração máxima em minutos. Ausência representa nenhum máximo configurado.",
            example = "60")
        Integer maxMinutes) {

  @AssertTrue(message = "A duração máxima deve ser maior ou igual à mínima.")
  @JsonIgnore
  @Schema(hidden = true)
  public boolean isOrdered() {
    return minMinutes == null || maxMinutes == null || minMinutes <= maxMinutes;
  }

  boolean hasLimits() {
    return minMinutes != null || maxMinutes != null;
  }

  int minimum() {
    return minMinutes == null ? 1 : minMinutes;
  }

  boolean accepts(long minutes) {
    return minutes >= minimum() && (maxMinutes == null || minutes <= maxMinutes);
  }
}
