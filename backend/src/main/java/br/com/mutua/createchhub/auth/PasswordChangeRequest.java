package br.com.mutua.createchhub.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordChangeRequest(
    @NotBlank @Size(max = 128) String currentPassword,
    @NotBlank @Size(min = 12, max = 128) String newPassword) {
  @Override
  public String toString() {
    return "PasswordChangeRequest[redacted]";
  }
}
