package br.com.mutua.createchhub.block;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.UUID;

public record SpaceBlockRequest(
    @NotNull UUID spaceId,
    @NotBlank
        @Pattern(
            regexp = TIMESTAMP_PATTERN,
            message = "Use o formato aaaa-MM-ddTHH:mm, sem fuso ou segundos.")
        @Schema(description = "Data e hora locais do Hub no Piauí.", example = "2027-03-10T09:15")
        String startsAt,
    @NotBlank
        @Pattern(
            regexp = TIMESTAMP_PATTERN,
            message = "Use o formato aaaa-MM-ddTHH:mm, sem fuso ou segundos.")
        @Schema(
            description = "Fim exclusivo, em data e hora locais do Hub no Piauí.",
            example = "2027-03-10T12:30")
        String endsAt,
    @NotBlank(message = "Informe o motivo do bloqueio.") @Size(max = 500) String reason,
    @Pattern(regexp = "(?!0000)[0-9]{4}-[0-9]{2}-[0-9]{2}")
        @Schema(
            description = "Última data inclusiva para inícios de uma série semanal.",
            example = "2027-04-28")
        String repeatUntil) {

  public static final String TIMESTAMP_PATTERN =
      "^(?!0000)[0-9]{4}-(?:0[1-9]|1[0-2])-(?:0[1-9]|[12][0-9]|3[01])T(?:[01][0-9]|2[0-3]):[0-5][0-9]$";

  @AssertTrue(message = "Informe datas válidas e um fim posterior ao início.")
  @Schema(hidden = true)
  public boolean isIntervalValid() {
    if (startsAt == null
        || endsAt == null
        || !startsAt.matches(TIMESTAMP_PATTERN)
        || !endsAt.matches(TIMESTAMP_PATTERN)) {
      return true;
    }
    try {
      return LocalDateTime.parse(endsAt).isAfter(LocalDateTime.parse(startsAt));
    } catch (DateTimeParseException exception) {
      return false;
    }
  }
}
