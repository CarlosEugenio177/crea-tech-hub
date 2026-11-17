package br.com.mutua.createchhub.space;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.UUID;

@Schema(
    name = "StationPlacement",
    requiredProperties = {"roomId", "number", "x", "y"})
public record StationPlacementResponse(UUID roomId, int number, BigDecimal x, BigDecimal y) {

  static StationPlacementResponse from(Space station) {
    return station.getRoomId() == null
        ? null
        : new StationPlacementResponse(
            station.getRoomId(), station.getStationNumber(), station.getMapX(), station.getMapY());
  }
}
