package br.com.mutua.createchhub.space;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpaceRepository extends JpaRepository<Space, UUID> {

  Page<Space> findByActive(boolean active, Pageable pageable);
}
