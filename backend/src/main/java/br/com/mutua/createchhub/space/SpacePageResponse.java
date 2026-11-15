package br.com.mutua.createchhub.space;

import java.util.List;
import org.springframework.data.domain.Page;

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
