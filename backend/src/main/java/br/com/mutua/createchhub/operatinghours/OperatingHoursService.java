package br.com.mutua.createchhub.operatinghours;

import java.time.LocalTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class OperatingHoursService {

  private final OperatingHoursRepository hours;

  public OperatingHoursService(OperatingHoursRepository hours) {
    this.hours = hours;
  }

  public OperatingHoursResponse current() {
    return OperatingHoursResponse.from(hours.findById(1).orElseThrow(this::missingConfiguration));
  }

  @Transactional
  public OperatingHoursResponse update(OperatingHoursRequest request) {
    var configuration = hours.findForUpdate().orElseThrow(this::missingConfiguration);
    configuration.update(
        request.daysOfWeek(),
        LocalTime.parse(request.openingTime()),
        LocalTime.parse(request.closingTime()));
    return OperatingHoursResponse.from(configuration);
  }

  private IllegalStateException missingConfiguration() {
    return new IllegalStateException("Funcionamento do Hub não foi inicializado pela migração.");
  }
}
