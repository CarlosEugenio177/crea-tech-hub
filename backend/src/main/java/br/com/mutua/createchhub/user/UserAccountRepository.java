package br.com.mutua.createchhub.user;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserAccountRepository extends JpaRepository<UserAccount, UUID> {

  @EntityGraph(attributePaths = "permissions")
  Optional<UserAccount> findByEmailAndAccessRole(String email, AccessRole accessRole);

  boolean existsByAccessRole(AccessRole accessRole);

  boolean existsByEmail(String email);

  Page<UserAccount> findByAccessRole(AccessRole role, Pageable pageable);

  @Query(
      "select count(a) from UserAccount a join a.permissions p where a.accessRole = :role and a.active = true and p = :permission")
  long countActiveWithPermission(AccessRole role, AdminPermission permission);
}
