package br.com.mutua.createchhub.space;

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
    BookingMode bookingMode) {

  static PublicSpaceResponse from(Space space) {
    return new PublicSpaceResponse(
        space.getId(),
        space.getName(),
        space.getType(),
        space.getCapacity(),
        space.getDescription(),
        space.getBookingMode());
  }
}
