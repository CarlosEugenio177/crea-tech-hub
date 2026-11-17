package br.com.mutua.createchhub.operatinghours;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.DayOfWeek;
import java.util.List;

@Schema(
    name = "OperatingHours",
    requiredProperties = {"daysOfWeek", "openingTime", "closingTime"})
public record OperatingHoursResponse(
    List<DayOfWeek> daysOfWeek, String openingTime, String closingTime) {

  public OperatingHoursResponse {
    daysOfWeek = List.copyOf(daysOfWeek);
  }

  static OperatingHoursResponse from(HubOperatingHours hours) {
    return new OperatingHoursResponse(
        hours.getDaysOfWeek().stream().sorted().toList(),
        hours.getOpeningTime().toString(),
        hours.getClosingTime().toString());
  }
}
