package br.com.mutua.createchhub.space;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(requiredProperties = {"closed", "periods"})
public record SpaceDayAgenda(boolean closed, List<Period> periods) {
  public SpaceDayAgenda {
    periods = List.copyOf(periods);
  }

  @Schema(
      name = "SpaceDayPeriod",
      requiredProperties = {"start", "end", "available"})
  public record Period(
      @Schema(example = "09:30") String start,
      @Schema(example = "10:45") String end,
      boolean available) {}
}
