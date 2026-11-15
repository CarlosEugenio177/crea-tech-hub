package br.com.mutua.createchhub.user;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SourceType;

@Entity
@Table(name = "user_accounts")
public class UserAccount {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @NotBlank
  @Size(max = 150)
  @Column(name = "full_name", nullable = false, length = 150)
  private String fullName;

  @NotBlank
  @Email
  @Size(max = 254)
  @Column(nullable = false, length = 254, unique = true)
  private String email;

  @NotBlank
  @Size(max = 255)
  @Column(name = "password_hash", nullable = false, length = 255)
  private String passwordHash;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(name = "access_role", nullable = false, length = 32)
  private AccessRole accessRole;

  @Column(nullable = false)
  private boolean active = true;

  @ElementCollection
  @CollectionTable(
      name = "user_account_permissions",
      joinColumns = @JoinColumn(name = "account_id"))
  @Enumerated(EnumType.STRING)
  @Column(name = "permission", nullable = false, length = 64)
  @BatchSize(size = 100)
  private Set<AdminPermission> permissions = new HashSet<>();

  @Column(name = "security_version", nullable = false)
  private long securityVersion;

  @Column(name = "password_change_required", nullable = false)
  private boolean passwordChangeRequired;

  @CreationTimestamp(source = SourceType.DB)
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  protected UserAccount() {}

  public UserAccount(String fullName, String email, String passwordHash, AccessRole accessRole) {
    this.fullName = fullName;
    this.email = email == null ? null : email.strip().toLowerCase(Locale.ROOT);
    this.passwordHash = passwordHash;
    this.accessRole = accessRole;
  }

  public UUID getId() {
    return id;
  }

  public String getFullName() {
    return fullName;
  }

  public String getEmail() {
    return email;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public AccessRole getAccessRole() {
    return accessRole;
  }

  public boolean isActive() {
    return active;
  }

  public void deactivate() {
    changeStatus(false);
  }

  public void changeStatus(boolean active) {
    if (this.active != active) {
      this.active = active;
      securityVersion++;
    }
  }

  public Set<AdminPermission> getPermissions() {
    return Set.copyOf(permissions);
  }

  public void replacePermissions(Set<AdminPermission> permissions) {
    if (!this.permissions.equals(permissions)) {
      this.permissions.clear();
      this.permissions.addAll(permissions);
      securityVersion++;
    }
  }

  public long getSecurityVersion() {
    return securityVersion;
  }

  public boolean isPasswordChangeRequired() {
    return passwordChangeRequired;
  }

  public void requirePasswordChange() {
    passwordChangeRequired = true;
  }

  public void changePassword(String passwordHash) {
    this.passwordHash = passwordHash;
    passwordChangeRequired = false;
    securityVersion++;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
