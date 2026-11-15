package br.com.mutua.createchhub.auth;

import br.com.mutua.createchhub.access.AccessAudit;
import br.com.mutua.createchhub.access.AdminAccessGuard;
import br.com.mutua.createchhub.access.AdminAccessPolicy;
import br.com.mutua.createchhub.shared.error.InvalidRequestException;
import java.util.UUID;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminPasswordService {

  private final AdminAccessGuard guard;
  private final AdminAccessPolicy policy;
  private final PasswordEncoder passwords;
  private final AccessAudit audit;

  public AdminPasswordService(
      AdminAccessGuard guard,
      AdminAccessPolicy policy,
      PasswordEncoder passwords,
      AccessAudit audit) {
    this.guard = guard;
    this.policy = policy;
    this.passwords = passwords;
    this.audit = audit;
  }

  @Transactional
  public void change(UUID id, PasswordChangeRequest request) {
    guard.lock();
    var account = policy.findAdmin(id);
    if (!account.isActive())
      throw new BadCredentialsException("Conta administrativa indisponível.");
    if (!passwords.matches(request.currentPassword(), account.getPasswordHash())) {
      throw new InvalidRequestException("Senha atual inválida.");
    }
    if (request.currentPassword().equals(request.newPassword())) {
      throw new InvalidRequestException("Escolha uma senha diferente da atual.");
    }
    account.changePassword(passwords.encode(request.newPassword()));
    audit.record(id, id, "PASSWORD_CHANGED", "Senha alterada pelo titular.");
  }
}
