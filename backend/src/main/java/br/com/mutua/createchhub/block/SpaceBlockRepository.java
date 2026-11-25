package br.com.mutua.createchhub.block;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface SpaceBlockRepository
    extends JpaRepository<SpaceBlock, UUID>, JpaSpecificationExecutor<SpaceBlock> {

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select b from SpaceBlock b where b.id = :id")
  Optional<SpaceBlock> findForUpdate(UUID id);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select b from SpaceBlock b where b.seriesId = :seriesId order by b.id")
  List<SpaceBlock> findSeriesForUpdate(UUID seriesId);

  @Query(
      """
      select distinct b.spaceId from SpaceBlock b
      where b.spaceId in :ids and b.cancelledAt is null
      and b.startsAt < :end and b.endsAt > :start
      """)
  Set<UUID> findBlockedSpaceIds(List<UUID> ids, LocalDateTime start, LocalDateTime end);
}
