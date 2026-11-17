package br.com.mutua.createchhub.operatinghours;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface OperatingHoursRepository extends JpaRepository<HubOperatingHours, Integer> {

  @Override
  @EntityGraph(attributePaths = "daysOfWeek")
  Optional<HubOperatingHours> findById(Integer id);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select h from HubOperatingHours h where h.id = 1")
  Optional<HubOperatingHours> findForUpdate();
}
