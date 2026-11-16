package br.com.mutua.createchhub.access;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.springframework.data.domain.Page;

@Schema(
    name = "AdminTeamPage",
    requiredProperties = {"items", "page", "size", "totalElements", "totalPages"})
public record AdminTeamPage(
    List<AdminTeamResponse> items, int page, int size, long totalElements, int totalPages) {
  public AdminTeamPage {
    items = List.copyOf(items);
  }

  static AdminTeamPage from(Page<AdminTeamResponse> result) {
    return new AdminTeamPage(
        result.getContent(),
        result.getNumber(),
        result.getSize(),
        result.getTotalElements(),
        result.getTotalPages());
  }
}
