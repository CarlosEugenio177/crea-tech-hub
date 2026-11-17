package br.com.mutua.createchhub.block;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.springframework.data.domain.Page;

@Schema(requiredProperties = {"items", "page", "size", "totalElements", "totalPages"})
public record SpaceBlockPage(
    List<SpaceBlockResponse> items, int page, int size, long totalElements, int totalPages) {

  public SpaceBlockPage {
    items = List.copyOf(items);
  }

  static SpaceBlockPage from(Page<SpaceBlock> page) {
    return new SpaceBlockPage(
        page.getContent().stream().map(SpaceBlockResponse::from).toList(),
        page.getNumber(),
        page.getSize(),
        page.getTotalElements(),
        page.getTotalPages());
  }
}
