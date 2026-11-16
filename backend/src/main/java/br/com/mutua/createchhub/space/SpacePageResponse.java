package br.com.mutua.createchhub.space;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.springframework.data.domain.Page;

@Schema(
    name = "SpacePage",
    requiredProperties = {"items", "page", "size", "totalElements", "totalPages"})
public record SpacePageResponse(
    List<SpaceResponse> items, int page, int size, long totalElements, int totalPages) {

  public SpacePageResponse {
    items = List.copyOf(items);
  }

  static SpacePageResponse from(Page<SpaceResponse> result) {
    return new SpacePageResponse(
        result.getContent(),
        result.getNumber(),
        result.getSize(),
        result.getTotalElements(),
        result.getTotalPages());
  }
}
