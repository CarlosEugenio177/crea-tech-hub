package br.com.mutua.createchhub.access;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;

@Schema(
    name = "AdminAuditPage",
    requiredProperties = {"items", "page", "size", "totalElements", "totalPages"})
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

  @Schema(
      name = "AccessAuditEvent",
      requiredProperties = {"id", "targetId", "action", "detail", "createdAt"})
  public record Event(
      UUID id,
      @Schema(
              types = {"string", "null"},
              format = "uuid")
          UUID actorId,
      UUID targetId,
      String action,
      String detail,
      Instant createdAt) {}
}
