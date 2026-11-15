package br.com.mutua.createchhub.access;

import br.com.mutua.createchhub.shared.error.ResourceConflictException;
import br.com.mutua.createchhub.user.AccessRole;
import br.com.mutua.createchhub.user.AdminPermission;
import br.com.mutua.createchhub.user.UserAccount;
import br.com.mutua.createchhub.user.UserAccountRepository;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AdminTeamService {

  private final UserAccountRepository accounts;
  private final AdminAccessPolicy policy;
  private final PasswordEncoder passwords;
  private final AccessAudit audit;
  private final AccessAuditRepository events;

  public AdminTeamService(
      UserAccountRepository accounts,
      AdminAccessPolicy policy,
      PasswordEncoder passwords,
      AccessAudit audit,
      AccessAuditRepository events) {
    this.accounts = accounts;
    this.policy = policy;
    this.passwords = passwords;
    this.audit = audit;
    this.events = events;
  }

  public AdminTeamPage list(int page, int size) {
    return AdminTeamPage.from(
        accounts
            .findByAccessRole(
                AccessRole.ADMIN, PageRequest.of(page, size, Sort.by("fullName", "id")))
            .map(AdminTeamResponse::from));
  }

  public AdminAuditPage audit(int page, int size) {
    return AdminAuditPage.from(
        events.findAll(PageRequest.of(page, size, Sort.by("createdAt", "id").descending())));
  }

  public AdminTeamResponse find(UUID id) {
    return AdminTeamResponse.from(policy.findAdmin(id));
  }

  @Transactional
  public AdminTeamResponse create(UUID actorId, AdminCreateRequest request) {
    policy.requireManager(actorId);
    var email = request.email().strip().toLowerCase(Locale.ROOT);
    if (accounts.existsByEmail(email)) {
      throw new ResourceConflictException("Já existe uma conta com esse e-mail.");
    }
    var account =
        new UserAccount(
            request.fullName().strip(),
            email,
            passwords.encode(request.initialPassword()),
            AccessRole.ADMIN);
    account.replacePermissions(request.permissions());
    account.requirePasswordChange();
    accounts.saveAndFlush(account);
    audit.record(
        actorId,
        account.getId(),
        "ADMIN_CREATED",
        "Permissões: " + permissionNames(account.getPermissions()));
    return AdminTeamResponse.from(account);
  }

  @Transactional
  public AdminTeamResponse permissions(
      UUID actorId, UUID targetId, Set<AdminPermission> permissions) {
    policy.requireManager(actorId);
    var target = policy.findAdmin(targetId);
    policy.checkChange(actorId, target, target.isActive(), permissions);
    var previous = permissionNames(target.getPermissions());
    target.replacePermissions(permissions);
    audit.record(
        actorId, targetId, "PERMISSIONS_CHANGED", previous + " -> " + permissionNames(permissions));
    return AdminTeamResponse.from(target);
  }

  @Transactional
  public AdminTeamResponse status(UUID actorId, UUID targetId, boolean active) {
    policy.requireManager(actorId);
    var target = policy.findAdmin(targetId);
    policy.checkChange(actorId, target, active, target.getPermissions());
    var previous = target.isActive();
    target.changeStatus(active);
    audit.record(actorId, targetId, "STATUS_CHANGED", previous + " -> " + active);
    return AdminTeamResponse.from(target);
  }

  private String permissionNames(Set<AdminPermission> permissions) {
    return permissions.stream().map(Enum::name).sorted().toList().toString();
  }
}
