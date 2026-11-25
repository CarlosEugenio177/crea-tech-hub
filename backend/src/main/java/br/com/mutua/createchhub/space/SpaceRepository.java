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
  @Query("select s.roomId from Space s where s.id = :id")
  UUID findParentId(UUID id);

  Page<Space> findByActive(boolean active, Pageable pageable);

  @Query(
      """
      select s from Space s where s.active = true
        and (s.roomId is null or (:topLevelOnly = false and exists
          (select r.id from Space r where r.id = s.roomId and r.active = true)))
      """)
  Page<Space> findPublic(boolean topLevelOnly, Pageable pageable);

  Page<Space> findByRoomId(UUID roomId, Pageable pageable);

  Page<Space> findByRoomIdAndActiveTrue(UUID roomId, Pageable pageable);

  List<Space> findByRoomIdAndActiveTrueOrderByStationNumber(UUID roomId);

  List<Space> findByRoomIdInAndActiveTrue(List<UUID> roomIds);

  @Query(
      """
      select s from Space s
      where s.active = true and s.roomId is null
        and ((s.bookingMode = br.com.mutua.createchhub.space.BookingMode.WHOLE_SPACE
            and s.capacity >= :participants)
          or (s.bookingMode = br.com.mutua.createchhub.space.BookingMode.PER_STATION
            and :participants = 1))
      """)
  List<Space> findRecommendationCandidates(int participants);

  boolean existsByRoomId(UUID roomId);

  @Query("select max(s.stationNumber) from Space s where s.roomId = :roomId")
  Integer findLastStationNumber(UUID roomId);

  boolean existsByRoomIdAndStationNumberAndIdNot(UUID roomId, int stationNumber, UUID id);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select s from Space s where s.id = :id")
  Optional<Space> findForUpdate(UUID id);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select s from Space s where s.id in :ids order by s.id")
  List<Space> findAllForUpdate(List<UUID> ids);
}
