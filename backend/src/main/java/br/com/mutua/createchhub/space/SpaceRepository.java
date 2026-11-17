package br.com.mutua.createchhub.space;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface SpaceRepository extends JpaRepository<Space, UUID> {

  Page<Space> findByActive(boolean active, Pageable pageable);

  Page<Space> findByRoomId(UUID roomId, Pageable pageable);

  Page<Space> findByRoomIdAndActiveTrue(UUID roomId, Pageable pageable);

  boolean existsByRoomId(UUID roomId);

  boolean existsByRoomIdAndStationNumberAndIdNot(UUID roomId, int stationNumber, UUID id);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select s from Space s where s.id = :id")
  Optional<Space> findForUpdate(UUID id);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select s from Space s where s.id in :ids order by s.id")
  List<Space> findAllForUpdate(List<UUID> ids);
}
