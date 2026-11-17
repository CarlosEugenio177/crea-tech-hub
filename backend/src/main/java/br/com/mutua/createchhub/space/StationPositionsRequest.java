package br.com.mutua.createchhub.space;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record StationPositionsRequest(
    @NotNull @Size(min = 1, max = 1000) List<@NotNull @Valid Position> positions) {

  public StationPositionsRequest {
    positions = List.copyOf(positions);
  }

  @Schema(name = "StationPositionUpdate")
  public record Position(
      @NotNull UUID id,
      @NotNull @Min(0) Long version,
      @NotNull
          @DecimalMin("0")
          @DecimalMax("100")
          @Digits(integer = 3, fraction = 2)
          @Schema(multipleOf = 0.01)
          BigDecimal x,
      @NotNull
          @DecimalMin("0")
          @DecimalMax("100")
          @Digits(integer = 3, fraction = 2)
          @Schema(multipleOf = 0.01)
          BigDecimal y) {}
}
