package br.com.mutua.createchhub.recovery;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetToken {

  @Id
  @Column(name = "account_id")
  private UUID accountId;

  @Column(name = "token_hash", nullable = false, length = 64, unique = true)
  private String tokenHash;

  @Column(name = "security_version", nullable = false)
  private long securityVersion;

  @Column(name = "requested_at", nullable = false)
  private Instant requestedAt;

  @Column(name = "expires_at", nullable = false)
  private Instant expiresAt;

  protected PasswordResetToken() {}

  PasswordResetToken(UUID accountId, String hash, long version, Instant now) {
    this.accountId = accountId;
    tokenHash = hash;
    securityVersion = version;
    requestedAt = now;
    expiresAt = now.plusSeconds(900);
  }

  public UUID getAccountId() {
    return accountId;
  }

  public String getTokenHash() {
    return tokenHash;
  }

  public long getSecurityVersion() {
    return securityVersion;
  }

  public Instant getRequestedAt() {
    return requestedAt;
  }

  public Instant getExpiresAt() {
    return expiresAt;
  }
}
