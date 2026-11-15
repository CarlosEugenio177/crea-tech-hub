package br.com.mutua.createchhub.access;

import br.com.mutua.createchhub.user.AdminPermission;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Set;

public record AdminCreateRequest(
    @NotBlank @Size(max = 150) String fullName,
    @NotBlank @Email @Size(max = 254) String email,
    @NotBlank @Size(min = 12, max = 128) String initialPassword,
    @NotNull Set<@NotNull AdminPermission> permissions) {

  public AdminCreateRequest {
    permissions = Set.copyOf(permissions);
  }

  @Override
  public String toString() {
    return "AdminCreateRequest[redacted]";
  }
}
