package br.com.mutua.createchhub.auth;

import br.com.mutua.createchhub.user.AccessRole;
import br.com.mutua.createchhub.user.UserAccount;
import java.util.UUID;

public record AdminAccountResponse(UUID id, String fullName, String email, AccessRole role) {

  static AdminAccountResponse from(UserAccount account) {
    return new AdminAccountResponse(
        account.getId(), account.getFullName(), account.getEmail(), account.getAccessRole());
  }
}
