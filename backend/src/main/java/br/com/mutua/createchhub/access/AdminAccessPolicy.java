package br.com.mutua.createchhub.access;

import br.com.mutua.createchhub.shared.error.ResourceConflictException;
import br.com.mutua.createchhub.shared.error.ResourceNotFoundException;
import br.com.mutua.createchhub.user.AccessRole;
import br.com.mutua.createchhub.user.AdminPermission;
import br.com.mutua.createchhub.user.UserAccount;
import br.com.mutua.createchhub.user.UserAccountRepository;
import java.util.Set;
import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
public class AdminAccessPolicy {

  private final UserAccountRepository accounts;
  private final AdminAccessGuard guard;

  public AdminAccessPolicy(UserAccountRepository accounts, AdminAccessGuard guard) {
    this.accounts = accounts;
    this.guard = guard;
  }

  public void requireManager(UUID actorId) {
    guard.lock();
    var actor = findAdmin(actorId);
    if (!actor.isActive()
        || actor.isPasswordChangeRequired()
        || !actor.getPermissions().contains(AdminPermission.MANAGE_ACCESS)) {
      throw new AccessDeniedException("Gestão de acessos não autorizada.");
    }
  }

  public UserAccount findAdmin(UUID id) {
    return accounts
        .findById(id)
        .filter(account -> account.getAccessRole() == AccessRole.ADMIN)
        .orElseThrow(() -> new ResourceNotFoundException("Conta administrativa não encontrada."));
  }

  public void checkChange(
      UUID actorId, UserAccount target, boolean active, Set<AdminPermission> permissions) {
    if (actorId.equals(target.getId())) {
      throw new ResourceConflictException(
          "Peça a outro gestor para alterar suas permissões ou situação.");
    }
    var removesManager =
        target.isActive()
            && target.getPermissions().contains(AdminPermission.MANAGE_ACCESS)
            && (!active || !permissions.contains(AdminPermission.MANAGE_ACCESS));
    if (removesManager
        && accounts.countActiveWithPermission(AccessRole.ADMIN, AdminPermission.MANAGE_ACCESS)
            <= 1) {
      throw new ResourceConflictException(
          "É necessário manter pelo menos um gestor de acessos ativo.");
    }
  }
}
