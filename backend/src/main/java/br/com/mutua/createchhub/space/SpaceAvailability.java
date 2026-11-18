package br.com.mutua.createchhub.space;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;

@Schema(requiredProperties = {"status", "stations"})
public record SpaceAvailability(Status status, List<Station> stations) {
  public SpaceAvailability {
    stations = List.copyOf(stations);
  }

  @Schema(enumAsRef = true)
  public enum Status {
    AVAILABLE,
    CLOSED,
    UNAVAILABLE
  }

  @Schema(requiredProperties = {"id", "number", "x", "y", "available"})
  public record Station(UUID id, int number, double x, double y, boolean available) {}
}
