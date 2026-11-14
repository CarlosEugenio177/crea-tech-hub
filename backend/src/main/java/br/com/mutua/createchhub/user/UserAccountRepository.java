package br.com.mutua.createchhub.user;

import java.util.UUID;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAccountRepository extends JpaRepository<UserAccount, UUID> {

	Optional<UserAccount> findByEmailAndAccessRole(String email, AccessRole accessRole);

	boolean existsByAccessRole(AccessRole accessRole);
}
