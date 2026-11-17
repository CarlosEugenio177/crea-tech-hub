package br.com.mutua.createchhub.block;

import br.com.mutua.createchhub.auth.AdminPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/admin/space-blocks")
@PreAuthorize("hasAuthority('MANAGE_SPACES')")
public class SpaceBlockController {

  private final SpaceBlockService blocks;

  public SpaceBlockController(SpaceBlockService blocks) {
    this.blocks = blocks;
  }

  @Operation(operationId = "listSpaceBlocks", summary = "Consultar bloqueios de espaços")
  @GetMapping
  public SpaceBlockPage list(
      @Parameter(description = "Inclui bloqueios da sala quando o espaço é uma estação vinculada.")
          @RequestParam(required = false)
          UUID spaceId,
      @RequestParam(required = false) Boolean active,
      @RequestParam(required = false) @Pattern(regexp = SpaceBlockRequest.TIMESTAMP_PATTERN)
          String from,
      @RequestParam(required = false) @Pattern(regexp = SpaceBlockRequest.TIMESTAMP_PATTERN)
          String to,
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
    return blocks.list(spaceId, active, from, to, page, size);
  }

  @Operation(operationId = "getSpaceBlock", summary = "Consultar um bloqueio")
  @GetMapping("/{id}")
  public SpaceBlockResponse find(@PathVariable UUID id) {
    return blocks.find(id);
  }

  @Operation(operationId = "createSpaceBlock", summary = "Criar bloqueio de espaço")
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ResponseEntity<SpaceBlockResponse> create(
      @Valid @RequestBody SpaceBlockRequest request,
      @AuthenticationPrincipal AdminPrincipal principal) {
    var block = blocks.create(request, principal.getAccountId());
    var location =
        ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(block.id())
            .toUri();
    return ResponseEntity.created(location).body(block);
  }

  @Operation(
      operationId = "cancelSpaceBlock",
      summary = "Cancelar bloqueio preservando o histórico")
  @PostMapping("/{id}/cancel")
  public SpaceBlockResponse cancel(
      @PathVariable UUID id, @AuthenticationPrincipal AdminPrincipal principal) {
    return blocks.cancel(id, principal.getAccountId());
  }
}
