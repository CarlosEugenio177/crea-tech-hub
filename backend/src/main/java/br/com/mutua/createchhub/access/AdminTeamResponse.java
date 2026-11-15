package br.com.mutua.createchhub.access;

import br.com.mutua.createchhub.user.AccessRole;
import br.com.mutua.createchhub.user.AdminPermission;
import br.com.mutua.createchhub.user.UserAccount;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record AdminTeamResponse(
    UUID id,
    String fullName,
    String email,
    AccessRole role,
    boolean active,
    Set<AdminPermission> permissions,
    boolean passwordChangeRequired,
    Instant createdAt) {

  public AdminTeamResponse {
    permissions = Set.copyOf(permissions);
  }

  static AdminTeamResponse from(UserAccount account) {
    return new AdminTeamResponse(
        account.getId(),
        account.getFullName(),
        account.getEmail(),
        account.getAccessRole(),
        account.isActive(),
        account.getPermissions(),
        account.isPasswordChangeRequired(),
        account.getCreatedAt());
  }
}
