package br.com.mutua.createchhub.block;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(
    name = "SpaceBlock",
    requiredProperties = {
      "id",
      "spaceId",
      "startsAt",
      "endsAt",
      "reason",
      "createdBy",
      "createdAt",
      "active"
    })
public record SpaceBlockResponse(
    UUID id,
    UUID spaceId,
    @Schema(pattern = SpaceBlockRequest.TIMESTAMP_PATTERN) String startsAt,
    @Schema(pattern = SpaceBlockRequest.TIMESTAMP_PATTERN) String endsAt,
    String reason,
    UUID createdBy,
    Instant createdAt,
    boolean active,
    UUID cancelledBy,
    Instant cancelledAt) {

  static SpaceBlockResponse from(SpaceBlock block) {
    return new SpaceBlockResponse(
        block.getId(),
        block.getSpaceId(),
        block.getStartsAt().toString(),
        block.getEndsAt().toString(),
        block.getReason(),
        block.getCreatedBy(),
        block.getCreatedAt(),
        block.getCancelledAt() == null,
        block.getCancelledBy(),
        block.getCancelledAt());
  }
}
