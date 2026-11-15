package br.com.mutua.createchhub.space;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@PreAuthorize("hasAuthority('MANAGE_SPACES')")
@RequestMapping("/api/v1/admin/spaces")
public class SpaceController {

  private final SpaceService spaces;

  public SpaceController(SpaceService spaces) {
    this.spaces = spaces;
  }

  @GetMapping
  public SpacePageResponse list(
      @RequestParam(required = false) Boolean active,
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
    return SpacePageResponse.from(spaces.list(active, page, size));
  }

  @GetMapping("/{id}")
  public SpaceResponse find(@PathVariable UUID id) {
    return spaces.find(id);
  }

  @PostMapping
  public ResponseEntity<SpaceResponse> create(@Valid @RequestBody SpaceRequest request) {
    var space = spaces.create(request);
    var location =
        ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(space.id())
            .toUri();
    return ResponseEntity.created(location).body(space);
  }

  @PutMapping("/{id}")
  public SpaceResponse update(@PathVariable UUID id, @Valid @RequestBody SpaceRequest request) {
    return spaces.update(id, request);
  }

  @PatchMapping("/{id}/status")
  public SpaceResponse changeStatus(
      @PathVariable UUID id, @Valid @RequestBody SpaceStatusRequest request) {
    return spaces.changeStatus(id, request.active());
  }
}
