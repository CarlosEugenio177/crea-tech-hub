package br.com.mutua.createchhub.space;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.springframework.data.domain.Page;

@Schema(
    name = "PublicSpacePage",
    requiredProperties = {"items", "page", "size", "totalElements", "totalPages"})
public record PublicSpacePageResponse(
    List<PublicSpaceResponse> items, int page, int size, long totalElements, int totalPages) {

  public PublicSpacePageResponse {
    items = List.copyOf(items);
  }

  static PublicSpacePageResponse from(Page<PublicSpaceResponse> result) {
    return new PublicSpacePageResponse(
        result.getContent(),
        result.getNumber(),
        result.getSize(),
        result.getTotalElements(),
        result.getTotalPages());
  }
}
