package br.com.mutua.createchhub.access;

import java.util.List;
import org.springframework.data.domain.Page;

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
