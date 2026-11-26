package br.com.mutua.createchhub.space;

import br.com.mutua.createchhub.operatinghours.OperatingHoursResponse;
import br.com.mutua.createchhub.operatinghours.OperatingHoursService;
import br.com.mutua.createchhub.shared.error.InvalidRequestException;
import br.com.mutua.createchhub.shared.error.ResourceConflictException;
import br.com.mutua.createchhub.shared.error.ResourceNotFoundException;
import java.time.Clock;
import java.time.Duration;
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
    var interval = parseInterval(startsAt, endsAt);
    var space = findActive(id);
    var limits = bookingDuration(space);
    if (!isHubOpen(interval)) {
      return new SpaceAvailability(SpaceAvailability.Status.CLOSED, List.of());
    }
    if (!limits.accepts(interval.minutes())) {
      throw new ResourceConflictException(
          "O período escolhido está fora dos limites de duração deste espaço. Escolha outro horário.");
    }
    var stations = stations(space);
    var unavailable =
        intervals(space, stations, interval.start(), interval.end()).stream()
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
    var space = findActive(id);
    var limits = bookingDuration(space);
    OperatingHoursResponse schedule = hours.current();
    if (!schedule.daysOfWeek().contains(date.getDayOfWeek())) {
      return new SpaceDayAgenda(
          true,
          List.of(),
          space.getBookingMode(),
          List.of(),
          List.of(),
          limits.hasLimits() ? limits : null);
    }

    var open = date.atTime(LocalTime.parse(schedule.openingTime()));
    var close = date.atTime(LocalTime.parse(schedule.closingTime()));
    var bookableFrom = open.isAfter(now) ? open : now;
    var stations = stations(space);
    var intervals = intervals(space, stations, open, close);
    var sharedIds = sharedIds(space);
    var shared = intervals.stream().filter(item -> sharedIds.contains(item.resourceId())).toList();
    if (space.getBookingMode() != BookingMode.PER_STATION) {
      var windows = SpaceDayTimeline.freeWindows(bookableFrom, close, shared, limits.minimum());
      return new SpaceDayAgenda(
          false,
          SpaceDayTimeline.periods(open, close, windows),
          space.getBookingMode(),
          windows.stream().map(SpaceDayTimeline.Window::toResponse).toList(),
          List.of(),
          limits.hasLimits() ? limits : null);
    }
    var byStation =
        intervals.stream().collect(Collectors.groupingBy(SpaceBusyIntervals.Interval::resourceId));
    var allWindows = new ArrayList<SpaceDayTimeline.Window>();
    var dayStations = new ArrayList<SpaceDayAgenda.DayStation>();
    for (var station : stations) {
      var stationBusy = new ArrayList<>(shared);
      stationBusy.addAll(byStation.getOrDefault(station.getId(), List.of()));
      var windows =
          SpaceDayTimeline.freeWindows(bookableFrom, close, stationBusy, limits.minimum());
      allWindows.addAll(windows);
      dayStations.add(
          new SpaceDayAgenda.DayStation(
              station.getId(),
              station.getStationNumber(),
              station.getMapX().doubleValue(),
              station.getMapY().doubleValue(),
              windows.stream().map(SpaceDayTimeline.Window::toResponse).toList()));
    }
    return new SpaceDayAgenda(
        false,
        SpaceDayTimeline.periods(open, close, allWindows),
        space.getBookingMode(),
        List.of(),
        dayStations,
        limits.hasLimits() ? limits : null);
  }

  private List<SpaceBusyIntervals.Interval> intervals(
      Space space, List<Space> stations, LocalDateTime start, LocalDateTime end) {
    var ids = new ArrayList<>(sharedIds(space));
    stations.forEach(station -> ids.add(station.getId()));
    var wholeRooms = isWholeRoom(space) ? List.of(space.getId()) : List.<UUID>of();
    return busy.find(ids, wholeRooms, start, end);
  }

  static boolean isWholeRoom(Space space) {
    return space.getType() == SpaceType.ROOM && space.getBookingMode() == BookingMode.WHOLE_SPACE;
  }

  LocalInterval parseInterval(String startsAt, String endsAt) {
    var start = parse(startsAt);
    var end = parse(endsAt);
    if (!end.isAfter(start)) {
      throw new InvalidRequestException("O fim deve ser depois do início.");
    }
    if (start.isBefore(now())) {
      throw new InvalidRequestException("Escolha um horário a partir de agora.");
    }
    return new LocalInterval(start, end);
  }

  boolean isHubOpen(LocalInterval interval) {
    var schedule = hours.current();
    var start = interval.start();
    var end = interval.end();
    return start.toLocalDate().equals(end.toLocalDate())
        && schedule.daysOfWeek().contains(start.getDayOfWeek())
        && !start.toLocalTime().isBefore(LocalTime.parse(schedule.openingTime()))
        && !end.toLocalTime().isAfter(LocalTime.parse(schedule.closingTime()));
  }

  record LocalInterval(LocalDateTime start, LocalDateTime end) {
    long minutes() {
      return Duration.between(start, end).toMinutes();
    }
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

  private BookingDuration bookingDuration(Space space) {
    return space.getRoomId() == null
        ? space.bookingDuration()
        : findActive(space.getRoomId()).bookingDuration();
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
