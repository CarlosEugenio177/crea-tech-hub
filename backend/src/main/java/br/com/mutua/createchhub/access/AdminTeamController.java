package br.com.mutua.createchhub.access;

import br.com.mutua.createchhub.auth.AdminPrincipal;
import br.com.mutua.createchhub.user.AdminPermission;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/team")
@PreAuthorize("hasAuthority('MANAGE_ACCESS')")
public class AdminTeamController {

  private final AdminTeamService team;

  public AdminTeamController(AdminTeamService team) {
    this.team = team;
  }

  @GetMapping
  public AdminTeamPage list(
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
    return team.list(page, size);
  }

  @GetMapping("/permissions")
  public List<PermissionOption> permissions() {
    return Arrays.stream(AdminPermission.values())
        .map(p -> new PermissionOption(p, p.getDescription()))
        .toList();
  }

  @GetMapping("/audit")
  public AdminAuditPage audit(
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
    return team.audit(page, size);
  }

  @GetMapping("/{id}")
  public AdminTeamResponse find(@PathVariable UUID id) {
    return team.find(id);
  }

  @PostMapping
  public ResponseEntity<AdminTeamResponse> create(
      @AuthenticationPrincipal AdminPrincipal actor,
      @Valid @RequestBody AdminCreateRequest request) {
    var account = team.create(actor.getAccountId(), request);
    return ResponseEntity.created(URI.create("/api/v1/admin/team/" + account.id())).body(account);
  }

  @PutMapping("/{id}/permissions")
  public AdminTeamResponse permissions(
      @AuthenticationPrincipal AdminPrincipal actor,
      @PathVariable UUID id,
      @Valid @RequestBody AdminPermissionsRequest request) {
    return team.permissions(actor.getAccountId(), id, request.permissions());
  }

  @PatchMapping("/{id}/status")
  public AdminTeamResponse status(
      @AuthenticationPrincipal AdminPrincipal actor,
      @PathVariable UUID id,
      @Valid @RequestBody AdminStatusRequest request) {
    return team.status(actor.getAccountId(), id, request.active());
  }

  public record PermissionOption(AdminPermission permission, String description) {}
}
