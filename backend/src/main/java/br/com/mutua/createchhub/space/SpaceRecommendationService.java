package br.com.mutua.createchhub.space;

import br.com.mutua.createchhub.block.SpaceBlockRepository;
import br.com.mutua.createchhub.reservation.ReservationRepository;
import br.com.mutua.createchhub.shared.error.InvalidRequestException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SpaceRecommendationService {
  private static final Comparator<Space> BEST_FIT_ORDER =
      Comparator.comparingInt(SpaceRecommendationService::reservableCapacity)
          .thenComparing(Space::getName)
          .thenComparing(Space::getId);

  private final SpaceRepository spaces;
  private final SpaceBlockRepository blocks;
  private final ReservationRepository reservations;
  private final SpaceAvailabilityService availability;

  public SpaceRecommendationService(
      SpaceRepository spaces,
      SpaceBlockRepository blocks,
      ReservationRepository reservations,
      SpaceAvailabilityService availability) {
    this.spaces = spaces;
    this.blocks = blocks;
    this.reservations = reservations;
    this.availability = availability;
  }

  @Transactional(readOnly = true)
  public SpaceRecommendationResponse recommend(int participants, String startsAt, String endsAt) {
    if (participants < 1) {
      throw new InvalidRequestException("Informe pelo menos uma pessoa.");
    }
    var interval = availability.parseInterval(startsAt, endsAt);
    if (!availability.isHubOpen(interval)) {
      return SpaceRecommendationResponse.closed();
    }
    var candidates = spaces.findRecommendationCandidates(participants);
    if (candidates.isEmpty()) {
      return SpaceRecommendationResponse.unavailable();
    }
    var start = interval.start();
    var end = interval.end();
    var perStationRoomIds =
        candidates.stream()
            .filter(space -> space.getBookingMode() == BookingMode.PER_STATION)
            .map(Space::getId)
            .toList();
    Map<UUID, List<Space>> stationsByRoom =
        perStationRoomIds.isEmpty()
            ? Map.of()
            : spaces.findByRoomIdInAndActiveTrue(perStationRoomIds).stream()
                .collect(Collectors.groupingBy(Space::getRoomId));

    var allIds = new ArrayList<UUID>(candidates.size());
    candidates.forEach(space -> allIds.add(space.getId()));
    stationsByRoom.values().forEach(list -> list.forEach(station -> allIds.add(station.getId())));

    var blocked = new HashSet<>(blocks.findBlockedSpaceIds(allIds, start, end));
    blocked.addAll(reservations.occupied(allIds, start, end));

    var wholeSpaceRoomIds =
        candidates.stream()
            .filter(
                space ->
                    space.getBookingMode() == BookingMode.WHOLE_SPACE
                        && space.getType() == SpaceType.ROOM)
            .map(Space::getId)
            .toList();
    Set<UUID> roomsWithOccupiedStations = reservations.occupiedRooms(wholeSpaceRoomIds, start, end);

    var availableSpaces =
        candidates.stream()
            .filter(space -> isAvailable(space, blocked, stationsByRoom, roomsWithOccupiedStations))
            .sorted(BEST_FIT_ORDER)
            .map(PublicSpaceResponse::from)
            .toList();

    return availableSpaces.isEmpty()
        ? SpaceRecommendationResponse.unavailable()
        : SpaceRecommendationResponse.available(availableSpaces);
  }

  private static boolean isAvailable(
      Space space,
      Set<UUID> blocked,
      Map<UUID, List<Space>> stationsByRoom,
      Set<UUID> roomsWithOccupiedStations) {
    if (blocked.contains(space.getId())) {
      return false;
    }
    if (space.getBookingMode() == BookingMode.PER_STATION) {
      return stationsByRoom.getOrDefault(space.getId(), List.of()).stream()
          .anyMatch(station -> !blocked.contains(station.getId()));
    }
    return space.getType() != SpaceType.ROOM || !roomsWithOccupiedStations.contains(space.getId());
  }

  private static int reservableCapacity(Space space) {
    return space.getBookingMode() == BookingMode.PER_STATION ? 1 : space.getCapacity();
  }
}
