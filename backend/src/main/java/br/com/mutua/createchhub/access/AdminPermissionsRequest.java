package br.com.mutua.createchhub.access;

import br.com.mutua.createchhub.user.AdminPermission;
import jakarta.validation.constraints.NotNull;
import java.util.Set;

public record AdminPermissionsRequest(@NotNull Set<@NotNull AdminPermission> permissions) {
  public AdminPermissionsRequest {
    permissions = Set.copyOf(permissions);
  }
}
