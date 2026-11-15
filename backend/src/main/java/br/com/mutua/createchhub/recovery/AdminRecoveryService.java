package br.com.mutua.createchhub.recovery;

import br.com.mutua.createchhub.access.AccessAudit;
import br.com.mutua.createchhub.access.AdminAccessGuard;
import br.com.mutua.createchhub.shared.error.ResourceNotFoundException;
import br.com.mutua.createchhub.user.AccessRole;
import br.com.mutua.createchhub.user.AdminPermission;
import br.com.mutua.createchhub.user.UserAccountRepository;
import java.util.EnumSet;
import java.util.Locale;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminRecoveryService {

  private final UserAccountRepository accounts;
  private final AdminAccessGuard guard;
  private final PasswordEncoder passwords;
  private final AccessAudit audit;

  public AdminRecoveryService(
      UserAccountRepository accounts,
      AdminAccessGuard guard,
      PasswordEncoder passwords,
      AccessAudit audit) {
    this.accounts = accounts;
    this.guard = guard;
    this.passwords = passwords;
    this.audit = audit;
  }

  @Transactional
  public void recover(String email, String newPassword) {
    if (newPassword == null
        || newPassword.isBlank()
        || newPassword.length() < 12
        || newPassword.length() > 128) {
      throw new IllegalArgumentException("A senha de recuperação deve ter de 12 a 128 caracteres.");
    }
    guard.lock();
    var account =
        accounts
            .findByEmailAndAccessRole(email.strip().toLowerCase(Locale.ROOT), AccessRole.ADMIN)
            .orElseThrow(
                () -> new ResourceNotFoundException("Conta administrativa não encontrada."));
    account.changeStatus(true);
    account.replacePermissions(EnumSet.allOf(AdminPermission.class));
    account.changePassword(passwords.encode(newPassword));
    audit.record(
        null,
        account.getId(),
        "OPERATIONAL_RECOVERY",
        "Acesso recuperado por operação local autorizada.");
  }
}
