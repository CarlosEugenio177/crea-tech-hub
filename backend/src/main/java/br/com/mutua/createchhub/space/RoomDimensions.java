package br.com.mutua.createchhub.space;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(requiredProperties = {"width", "length"})
public record RoomDimensions(BigDecimal width, BigDecimal length) {}
