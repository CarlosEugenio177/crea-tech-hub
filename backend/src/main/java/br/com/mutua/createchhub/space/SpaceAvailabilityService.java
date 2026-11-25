package br.com.mutua.createchhub.space;

import br.com.mutua.createchhub.operatinghours.OperatingHoursResponse;
import br.com.mutua.createchhub.operatinghours.OperatingHoursService;
import br.com.mutua.createchhub.shared.error.InvalidRequestException;
import br.com.mutua.createchhub.shared.error.ResourceNotFoundException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class SpaceAvailabilityService {
  public static final ZoneId HUB_ZONE = ZoneId.of("America/Fortaleza");
  private final SpaceRepository spaces;
  private final SpaceBusyIntervals busy;
  private final OperatingHoursService hours;
  private final Clock clock;

  SpaceAvailabilityService(
      SpaceRepository spaces, SpaceBusyIntervals busy, OperatingHoursService hours, Clock clock) {
    this.spaces = spaces;
    this.busy = busy;
    this.hours = hours;
    this.clock = clock;
  }

  public SpaceAvailability check(UUID id, String startsAt, String endsAt) {
    var start = parse(startsAt);
    var end = parse(endsAt);
    if (!end.isAfter(start)) {
      throw new InvalidRequestException("O fim deve ser depois do início.");
    }
    if (start.isBefore(now())) {
      throw new InvalidRequestException("Escolha um horário a partir de agora.");
    }
    var space = publicSpace(id);
    var schedule = hours.current();
    if (!start.toLocalDate().equals(end.toLocalDate())
        || !schedule.daysOfWeek().contains(start.getDayOfWeek())
        || start.toLocalTime().isBefore(LocalTime.parse(schedule.openingTime()))
        || end.toLocalTime().isAfter(LocalTime.parse(schedule.closingTime()))) {
      return new SpaceAvailability(SpaceAvailability.Status.CLOSED, List.of());
    }
    var stations = stations(space);
    var unavailable =
        intervals(space, stations, start, end).stream()
            .map(SpaceBusyIntervals.Interval::resourceId)
            .collect(Collectors.toSet());
    boolean roomBlocked = sharedIds(space).stream().anyMatch(unavailable::contains);
    var options =
        stations.stream()
            .map(
                station ->
                    new SpaceAvailability.Station(
                        station.getId(),
                        station.getStationNumber(),
                        station.getMapX().doubleValue(),
                        station.getMapY().doubleValue(),
                        !roomBlocked && !unavailable.contains(station.getId())))
            .toList();
    boolean available =
        space.getBookingMode() == BookingMode.PER_STATION
            ? options.stream().anyMatch(SpaceAvailability.Station::available)
            : !roomBlocked;
    return new SpaceAvailability(
        available ? SpaceAvailability.Status.AVAILABLE : SpaceAvailability.Status.UNAVAILABLE,
        options);
  }

  public SpaceDayAgenda day(UUID id, LocalDate date) {
    var now = now();
    if (date.isBefore(now.toLocalDate())) {
      throw new InvalidRequestException("Escolha uma data a partir de hoje.");
    }
    var space = publicSpace(id);
    OperatingHoursResponse schedule = hours.current();
    if (!schedule.daysOfWeek().contains(date.getDayOfWeek())) {
      return new SpaceDayAgenda(true, List.of());
    }

    var open = date.atTime(LocalTime.parse(schedule.openingTime()));
    var close = date.atTime(LocalTime.parse(schedule.closingTime()));
    var bookableFrom = open.isAfter(now) ? open : now;
    var stations = stations(space);
    var intervals = intervals(space, stations, open, close);
    var sharedIds = sharedIds(space);
    var shared = intervals.stream().filter(item -> sharedIds.contains(item.resourceId())).toList();
    List<List<SpaceBusyIntervals.Interval>> byResource;
    if (space.getBookingMode() == BookingMode.PER_STATION) {
      var byStation =
          intervals.stream()
              .collect(Collectors.groupingBy(SpaceBusyIntervals.Interval::resourceId));
      byResource =
          stations.stream()
              .map(
                  station -> {
                    List<SpaceBusyIntervals.Interval> stationBusy = new ArrayList<>(shared);
                    stationBusy.addAll(byStation.getOrDefault(station.getId(), List.of()));
                    return stationBusy;
                  })
              .toList();
    } else {
      byResource = List.of(shared);
    }
    return new SpaceDayAgenda(
        false, SpaceDayTimeline.periods(open, close, bookableFrom, byResource));
  }

  private List<SpaceBusyIntervals.Interval> intervals(
      Space space, List<Space> stations, LocalDateTime start, LocalDateTime end) {
    var ids = new ArrayList<>(sharedIds(space));
    stations.forEach(station -> ids.add(station.getId()));
    boolean wholeRoom =
        space.getType() == SpaceType.ROOM && space.getBookingMode() == BookingMode.WHOLE_SPACE;
    return busy.find(ids, space.getId(), wholeRoom, start, end);
  }

  private List<UUID> sharedIds(Space space) {
    return space.getRoomId() == null
        ? List.of(space.getId())
        : List.of(space.getId(), space.getRoomId());
  }

  private List<Space> stations(Space space) {
    return space.getBookingMode() == BookingMode.PER_STATION
        ? spaces.findByRoomIdAndActiveTrueOrderByStationNumber(space.getId())
        : List.of();
  }

  private Space publicSpace(UUID id) {
    var space = findActive(id);
    if (space.getRoomId() != null) findActive(space.getRoomId());
    return space;
  }

  private Space findActive(UUID id) {
    return spaces
        .findById(id)
        .filter(Space::isActive)
        .orElseThrow(() -> new ResourceNotFoundException("Espaço não encontrado."));
  }

  private LocalDateTime now() {
    return LocalDateTime.now(clock.withZone(HUB_ZONE)).truncatedTo(ChronoUnit.MINUTES);
  }

  private LocalDateTime parse(String value) {
    try {
      return LocalDateTime.parse(value);
    } catch (DateTimeParseException exception) {
      throw new InvalidRequestException("Informe datas e horários válidos.");
    }
  }
}
