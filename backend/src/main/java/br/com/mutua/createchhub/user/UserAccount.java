package br.com.mutua.createchhub.user;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
	@Column(nullable = false, length = 254)
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

	@CreationTimestamp(source = SourceType.DB)
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	protected UserAccount() {
	}

	public UserAccount(String fullName, String email, String passwordHash, AccessRole accessRole) {
		this.fullName = fullName;
		this.email = email;
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

	public Instant getCreatedAt() {
		return createdAt;
	}
}
