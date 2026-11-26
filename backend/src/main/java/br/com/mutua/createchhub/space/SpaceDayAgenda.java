package br.com.mutua.createchhub.space;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;

@Schema(requiredProperties = {"closed", "periods", "bookingMode", "freeWindows", "stations"})
public record SpaceDayAgenda(
    boolean closed,
    List<Period> periods,
    BookingMode bookingMode,
    List<FreeWindow> freeWindows,
    List<DayStation> stations,
    @JsonInclude(JsonInclude.Include.NON_NULL) BookingDuration bookingDuration) {
  public SpaceDayAgenda {
    periods = List.copyOf(periods);
    freeWindows = List.copyOf(freeWindows);
    stations = List.copyOf(stations);
  }

  @Schema(
      name = "SpaceDayPeriod",
      requiredProperties = {"start", "end", "available"})
  public record Period(
      @Schema(example = "09:30") String start,
      @Schema(example = "10:45") String end,
      boolean available) {}

  @Schema(
      name = "SpaceFreeWindow",
      requiredProperties = {"start", "end"})
  public record FreeWindow(
      @Schema(example = "09:30") String start, @Schema(example = "10:45") String end) {}

  @Schema(
      name = "SpaceDayStation",
      requiredProperties = {"id", "number", "x", "y", "freeWindows"})
  public record DayStation(UUID id, int number, double x, double y, List<FreeWindow> freeWindows) {
    public DayStation {
      freeWindows = List.copyOf(freeWindows);
    }
  }
}
