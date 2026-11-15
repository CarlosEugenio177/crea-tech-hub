package br.com.mutua.createchhub.recovery;

import br.com.mutua.createchhub.access.AccessAudit;
import br.com.mutua.createchhub.access.AdminAccessGuard;
import br.com.mutua.createchhub.shared.error.InvalidRequestException;
import br.com.mutua.createchhub.user.AccessRole;
import br.com.mutua.createchhub.user.UserAccountRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PasswordResetService {

  private static final SecureRandom RANDOM = new SecureRandom();
  private final UserAccountRepository accounts;
  private final PasswordResetTokenRepository tokens;
  private final AdminAccessGuard guard;
  private final PasswordEncoder passwords;
  private final AccessAudit audit;
  private final Clock clock;

  public PasswordResetService(
      UserAccountRepository accounts,
      PasswordResetTokenRepository tokens,
      AdminAccessGuard guard,
      PasswordEncoder passwords,
      AccessAudit audit,
      Clock clock) {
    this.accounts = accounts;
    this.tokens = tokens;
    this.guard = guard;
    this.passwords = passwords;
    this.audit = audit;
    this.clock = clock;
  }

  @Transactional
  public Optional<ResetGrant> issue(String email) {
    guard.lock();
    var account =
        accounts.findByEmailAndAccessRole(email, AccessRole.ADMIN).filter(a -> a.isActive());
    if (account.isEmpty()) return Optional.empty();
    var target = account.orElseThrow();
    var now = Instant.now(clock);
    var previous = tokens.findById(target.getId());
    if (previous.isPresent()
        && previous.orElseThrow().getRequestedAt().plusSeconds(60).isAfter(now)) {
      return Optional.empty();
    }
    var bytes = new byte[32];
    RANDOM.nextBytes(bytes);
    var token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    tokens.save(
        new PasswordResetToken(target.getId(), hash(token), target.getSecurityVersion(), now));
    return Optional.of(new ResetGrant(target.getEmail(), token));
  }

  @Transactional
  public void reset(PasswordResetRequest request) {
    guard.lock();
    var token = tokens.findByTokenHash(hash(request.token())).orElseThrow(this::invalidLink);
    var account = accounts.findById(token.getAccountId()).orElseThrow(this::invalidLink);
    if (!token.getExpiresAt().isAfter(Instant.now(clock))
        || !account.isActive()
        || account.getAccessRole() != AccessRole.ADMIN
        || token.getSecurityVersion() != account.getSecurityVersion()) {
      throw invalidLink();
    }
    account.changePassword(passwords.encode(request.newPassword()));
    tokens.delete(token);
    audit.record(null, account.getId(), "PASSWORD_RESET", "Senha redefinida por link individual.");
  }

  private InvalidRequestException invalidLink() {
    return new InvalidRequestException("Link inválido ou expirado.");
  }

  static String hash(String token) {
    try {
      return HexFormat.of()
          .formatHex(
              MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException("SHA-256 indisponível.", exception);
    }
  }

  public record ResetGrant(String email, String token) {
    @Override
    public String toString() {
      return "ResetGrant[redacted]";
    }
  }
}
