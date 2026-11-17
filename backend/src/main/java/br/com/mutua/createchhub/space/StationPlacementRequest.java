package br.com.mutua.createchhub.space;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.UUID;

public record StationPlacementRequest(
    @NotNull UUID roomId,
    @NotNull @Positive Integer number,
    @NotNull
        @DecimalMin("0")
        @DecimalMax("100")
        @Digits(integer = 3, fraction = 2)
        @Schema(
            description = "Posição horizontal em porcentagem, a partir da esquerda.",
            example = "25.50",
            multipleOf = 0.01)
        BigDecimal x,
    @NotNull
        @DecimalMin("0")
        @DecimalMax("100")
        @Digits(integer = 3, fraction = 2)
        @Schema(
            description = "Posição vertical em porcentagem, a partir do topo.",
            example = "40.00",
            multipleOf = 0.01)
        BigDecimal y) {}
