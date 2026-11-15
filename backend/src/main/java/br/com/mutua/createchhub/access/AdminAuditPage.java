package br.com.mutua.createchhub.access;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;

public record AdminAuditPage(
    List<Event> items, int page, int size, long totalElements, int totalPages) {
  public AdminAuditPage {
    items = List.copyOf(items);
  }

  static AdminAuditPage from(Page<AccessAuditEvent> result) {
    var items =
        result.stream()
            .map(
                e ->
                    new Event(
                        e.getId(),
                        e.getActorId(),
                        e.getTargetId(),
                        e.getAction(),
                        e.getDetail(),
                        e.getCreatedAt()))
            .toList();
    return new AdminAuditPage(
        items,
        result.getNumber(),
        result.getSize(),
        result.getTotalElements(),
        result.getTotalPages());
  }

  public record Event(
      UUID id, UUID actorId, UUID targetId, String action, String detail, Instant createdAt) {}
}
