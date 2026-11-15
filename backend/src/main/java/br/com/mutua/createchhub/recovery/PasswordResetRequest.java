package br.com.mutua.createchhub.recovery;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PasswordResetRequest(
    @NotBlank @Pattern(regexp = "[A-Za-z0-9_-]{43}") String token,
    @NotBlank @Size(min = 12, max = 128) String newPassword) {
  @Override
  public String toString() {
    return "PasswordResetRequest[redacted]";
  }
}
