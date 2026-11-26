package br.com.mutua.createchhub.space;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(
    name = "PublicSpace",
    requiredProperties = {"id", "name", "type", "capacity", "bookingMode"})
public record PublicSpaceResponse(
    UUID id,
    String name,
    SpaceType type,
    int capacity,
    @Schema(types = {"string", "null"}) String description,
    BookingMode bookingMode,
    @JsonInclude(JsonInclude.Include.NON_NULL) RoomDimensions roomDimensions,
    @JsonInclude(JsonInclude.Include.NON_NULL) String photoUrl) {

  static PublicSpaceResponse from(Space space) {
    return new PublicSpaceResponse(
        space.getId(),
        space.getName(),
        space.getType(),
        space.getCapacity(),
        space.getDescription(),
        space.getBookingMode(),
        space.getRoomDimensions(),
        space.getPhotoUrl());
  }
}
