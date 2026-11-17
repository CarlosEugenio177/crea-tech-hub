package br.com.mutua.createchhub.operatinghours;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.DayOfWeek;
import java.util.Set;

public record OperatingHoursRequest(
    @NotEmpty(message = "Selecione pelo menos um dia de funcionamento.")
        Set<@NotNull DayOfWeek> daysOfWeek,
    @NotBlank
        @Pattern(
            regexp = TIME_PATTERN,
            message = "Informe a abertura no formato HH:mm, de 00:00 a 23:59.")
        @Schema(example = "08:00")
        String openingTime,
    @NotBlank
        @Pattern(
            regexp = TIME_PATTERN,
            message = "Informe o fechamento no formato HH:mm, de 00:00 a 23:59.")
        @Schema(example = "18:00")
        String closingTime) {

  private static final String TIME_PATTERN = "^(?:[01][0-9]|2[0-3]):[0-5][0-9]$";

  public OperatingHoursRequest {
    daysOfWeek = Set.copyOf(daysOfWeek);
  }

  @AssertTrue(message = "O fechamento deve ser depois da abertura, no mesmo dia.")
  @Schema(hidden = true)
  public boolean isIntervalValid() {
    return openingTime == null
        || closingTime == null
        || !openingTime.matches(TIME_PATTERN)
        || !closingTime.matches(TIME_PATTERN)
        || openingTime.compareTo(closingTime) < 0;
  }
}
