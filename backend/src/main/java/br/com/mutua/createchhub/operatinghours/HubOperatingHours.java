package br.com.mutua.createchhub.operatinghours;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "hub_operating_hours")
public class HubOperatingHours {

  @Id private Integer id;

  @NotNull
  @Column(name = "opening_time", nullable = false)
  private LocalTime openingTime;

  @NotNull
  @Column(name = "closing_time", nullable = false)
  private LocalTime closingTime;

  @NotEmpty
  @ElementCollection
  @CollectionTable(name = "hub_operating_days", joinColumns = @JoinColumn(name = "hub_id"))
  @Enumerated(EnumType.STRING)
  @Column(name = "day_of_week", nullable = false, length = 9)
  private Set<DayOfWeek> daysOfWeek = new HashSet<>();

  protected HubOperatingHours() {}

  void update(Set<DayOfWeek> days, LocalTime openingTime, LocalTime closingTime) {
    daysOfWeek.clear();
    daysOfWeek.addAll(days);
    this.openingTime = openingTime;
    this.closingTime = closingTime;
  }

  public LocalTime getOpeningTime() {
    return openingTime;
  }

  public LocalTime getClosingTime() {
    return closingTime;
  }

  public Set<DayOfWeek> getDaysOfWeek() {
    return Set.copyOf(daysOfWeek);
  }
}
