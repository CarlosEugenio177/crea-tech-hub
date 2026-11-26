package br.com.mutua.createchhub.space;

import br.com.mutua.createchhub.shared.error.InvalidRequestException;
import java.util.ArrayList;
import java.util.Comparator;
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
  private final SpaceBusyIntervals busy;
  private final SpaceAvailabilityService availability;

  SpaceRecommendationService(
      SpaceRepository spaces, SpaceBusyIntervals busy, SpaceAvailabilityService availability) {
    this.spaces = spaces;
    this.busy = busy;
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
    var candidates =
        spaces.findRecommendationCandidates(participants).stream()
            .filter(space -> space.bookingDuration().accepts(interval.minutes()))
            .toList();
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

    var wholeRooms =
        candidates.stream()
            .filter(SpaceAvailabilityService::isWholeRoom)
            .map(Space::getId)
            .toList();
    var blocked =
        busy.find(allIds, wholeRooms, start, end).stream()
            .map(SpaceBusyIntervals.Interval::resourceId)
            .collect(Collectors.toSet());

    var availableSpaces =
        candidates.stream()
            .filter(space -> isAvailable(space, blocked, stationsByRoom))
            .sorted(BEST_FIT_ORDER)
            .map(PublicSpaceResponse::from)
            .toList();

    return availableSpaces.isEmpty()
        ? SpaceRecommendationResponse.unavailable()
        : SpaceRecommendationResponse.available(availableSpaces);
  }

  private static boolean isAvailable(
      Space space, Set<UUID> blocked, Map<UUID, List<Space>> stationsByRoom) {
    if (blocked.contains(space.getId())) {
      return false;
    }
    if (space.getBookingMode() == BookingMode.PER_STATION) {
      return stationsByRoom.getOrDefault(space.getId(), List.of()).stream()
          .anyMatch(station -> !blocked.contains(station.getId()));
    }
    return true;
  }

  private static int reservableCapacity(Space space) {
    return space.getBookingMode() == BookingMode.PER_STATION ? 1 : space.getCapacity();
  }
}
