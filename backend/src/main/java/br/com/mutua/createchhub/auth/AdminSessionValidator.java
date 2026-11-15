package br.com.mutua.createchhub.auth;

import br.com.mutua.createchhub.user.AccessRole;
import br.com.mutua.createchhub.user.UserAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AdminSessionValidator {

  private final UserAccountRepository accounts;

  public AdminSessionValidator(UserAccountRepository accounts) {
    this.accounts = accounts;
  }

  public SessionState check(AdminPrincipal principal) {
    return accounts
        .findById(principal.getAccountId())
        .filter(
            account ->
                account.isActive()
                    && account.getAccessRole() == AccessRole.ADMIN
                    && account.getSecurityVersion() == principal.getSecurityVersion())
        .map(
            account ->
                account.isPasswordChangeRequired()
                    ? SessionState.PASSWORD_CHANGE_REQUIRED
                    : SessionState.VALID)
        .orElse(SessionState.REVOKED);
  }

  public enum SessionState {
    VALID,
    PASSWORD_CHANGE_REQUIRED,
    REVOKED
  }
}
