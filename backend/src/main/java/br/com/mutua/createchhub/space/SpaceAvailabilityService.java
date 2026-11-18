package br.com.mutua.createchhub.space;

import br.com.mutua.createchhub.block.SpaceBlockRepository;
import br.com.mutua.createchhub.operatinghours.OperatingHoursService;
import br.com.mutua.createchhub.reservation.ReservationRepository;
import br.com.mutua.createchhub.shared.error.InvalidRequestException;
import br.com.mutua.createchhub.shared.error.ResourceNotFoundException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SpaceAvailabilityService {
  private static final ZoneId HUB_ZONE = ZoneId.of("America/Fortaleza");
  private final SpaceRepository spaces;
  private final SpaceBlockRepository blocks;
  private final OperatingHoursService hours;
  private final Clock clock;
  private final ReservationRepository reservations;

  public SpaceAvailabilityService(
      SpaceRepository spaces,
      SpaceBlockRepository blocks,
      OperatingHoursService hours,
      Clock clock,
      ReservationRepository reservations) {
    this.spaces = spaces;
    this.blocks = blocks;
    this.hours = hours;
    this.clock = clock;
    this.reservations = reservations;
  }

  @Transactional(readOnly = true)
  public SpaceAvailability check(UUID id, String startsAt, String endsAt) {
    var start = parse(startsAt);
    var end = parse(endsAt);
    if (!end.isAfter(start)) {
      throw new InvalidRequestException("O fim deve ser depois do início.");
    }
    if (start.isBefore(LocalDateTime.now(clock.withZone(HUB_ZONE)).withSecond(0).withNano(0))) {
      throw new InvalidRequestException("Escolha um horário a partir de agora.");
    }
    var space = findActive(id);
    if (space.getRoomId() != null) findActive(space.getRoomId());
    var schedule = hours.current();
    if (!start.toLocalDate().equals(end.toLocalDate())
        || !schedule.daysOfWeek().contains(start.getDayOfWeek())
        || start.toLocalTime().isBefore(LocalTime.parse(schedule.openingTime()))
        || end.toLocalTime().isAfter(LocalTime.parse(schedule.closingTime()))) {
      return new SpaceAvailability(SpaceAvailability.Status.CLOSED, List.of());
    }
    boolean perStation = space.getBookingMode() == BookingMode.PER_STATION;
    var stations =
        perStation ? spaces.findByRoomIdAndActiveTrueOrderByStationNumber(id) : List.<Space>of();
    var ids = new ArrayList<UUID>();
    ids.add(id);
    if (space.getRoomId() != null) ids.add(space.getRoomId());
    stations.forEach(station -> ids.add(station.getId()));
    var blocked = new HashSet<>(blocks.findBlockedSpaceIds(ids, start, end));
    blocked.addAll(reservations.occupied(ids, start, end));
    boolean roomBlocked =
        blocked.contains(id) || (space.getRoomId() != null && blocked.contains(space.getRoomId()));
    var options =
        stations.stream()
            .map(
                station ->
                    new SpaceAvailability.Station(
                        station.getId(),
                        station.getStationNumber(),
                        station.getMapX().doubleValue(),
                        station.getMapY().doubleValue(),
                        !roomBlocked && !blocked.contains(station.getId())))
            .toList();
    boolean available =
        perStation
            ? options.stream().anyMatch(SpaceAvailability.Station::available)
            : !roomBlocked
                && (space.getType() != SpaceType.ROOM
                    || !reservations.hasOccupiedStations(id, start, end));
    return new SpaceAvailability(
        available ? SpaceAvailability.Status.AVAILABLE : SpaceAvailability.Status.UNAVAILABLE,
        options);
  }

  private Space findActive(UUID id) {
    return spaces
        .findById(id)
        .filter(Space::isActive)
        .orElseThrow(() -> new ResourceNotFoundException("Espaço não encontrado."));
  }

  private LocalDateTime parse(String value) {
    try {
      return LocalDateTime.parse(value);
    } catch (DateTimeParseException exception) {
      throw new InvalidRequestException("Informe datas e horários válidos.");
    }
  }
}
