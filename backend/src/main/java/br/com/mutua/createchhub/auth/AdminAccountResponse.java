package br.com.mutua.createchhub.auth;

import br.com.mutua.createchhub.user.AccessRole;
import br.com.mutua.createchhub.user.AdminPermission;
import br.com.mutua.createchhub.user.UserAccount;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;
import java.util.UUID;

@Schema(
    name = "AdminAccount",
    requiredProperties = {
      "id",
      "fullName",
      "email",
      "role",
      "permissions",
      "passwordChangeRequired"
    })
public record AdminAccountResponse(
    UUID id,
    String fullName,
    String email,
    AccessRole role,
    Set<AdminPermission> permissions,
    boolean passwordChangeRequired) {

  public AdminAccountResponse {
    permissions = Set.copyOf(permissions);
  }

  static AdminAccountResponse from(UserAccount account) {
    return new AdminAccountResponse(
        account.getId(),
        account.getFullName(),
        account.getEmail(),
        account.getAccessRole(),
        account.getPermissions(),
        account.isPasswordChangeRequired());
  }
}
