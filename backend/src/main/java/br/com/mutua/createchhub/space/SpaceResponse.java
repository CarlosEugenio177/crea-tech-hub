package br.com.mutua.createchhub.space;

import java.time.Instant;
import java.util.UUID;

public record SpaceResponse(
    UUID id,
    String name,
    SpaceType type,
    int capacity,
    String description,
    boolean active,
    Instant createdAt) {

  static SpaceResponse from(Space space) {
    return new SpaceResponse(
        space.getId(),
        space.getName(),
        space.getType(),
        space.getCapacity(),
        space.getDescription(),
        space.isActive(),
        space.getCreatedAt());
  }
}
