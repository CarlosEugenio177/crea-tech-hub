package br.com.mutua.createchhub.space;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(
    name = "Space",
    requiredProperties = {
      "id",
      "name",
      "type",
      "capacity",
      "active",
      "createdAt",
      "bookingMode",
      "version"
    })
public record SpaceResponse(
    UUID id,
    String name,
    SpaceType type,
    int capacity,
    @Schema(types = {"string", "null"}) String description,
    boolean active,
    Instant createdAt,
    @JsonInclude(JsonInclude.Include.NON_NULL) StationPlacementResponse stationPlacement,
    BookingMode bookingMode,
    long version,
    @JsonInclude(JsonInclude.Include.NON_NULL) RoomDimensions roomDimensions) {

  static SpaceResponse from(Space space) {
    return new SpaceResponse(
        space.getId(),
        space.getName(),
        space.getType(),
        space.getCapacity(),
        space.getDescription(),
        space.isActive(),
        space.getCreatedAt(),
        StationPlacementResponse.from(space),
        space.getBookingMode(),
        space.getVersion(),
        space.getRoomDimensions());
  }
}
