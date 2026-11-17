package br.com.mutua.createchhub.space;

import br.com.mutua.createchhub.space.StationPositionsRequest.Position;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public record RoomLayoutRequest(
    @NotNull @Min(0) Long version,
    @NotNull
        @DecimalMin("1")
        @DecimalMax("100")
        @Digits(integer = 3, fraction = 2)
        @Schema(description = "Largura do mapa em metros", multipleOf = 0.01)
        BigDecimal width,
    @NotNull
        @DecimalMin("1")
        @DecimalMax("100")
        @Digits(integer = 3, fraction = 2)
        @Schema(description = "Comprimento do mapa em metros", multipleOf = 0.01)
        BigDecimal length,
    @NotNull @Size(max = 1000) List<@NotNull @Valid Position> positions,
    @NotNull @Size(max = 1000) List<@NotNull @Valid RemovedStation> removedStations) {

  public RoomLayoutRequest {
    positions = positions == null ? null : Collections.unmodifiableList(new ArrayList<>(positions));
    removedStations =
        removedStations == null
            ? null
            : Collections.unmodifiableList(new ArrayList<>(removedStations));
  }

  public record RemovedStation(@NotNull UUID id, @NotNull @Min(0) Long version) {}
}
