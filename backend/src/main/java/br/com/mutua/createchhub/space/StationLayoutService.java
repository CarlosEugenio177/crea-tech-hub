package br.com.mutua.createchhub.space;

import br.com.mutua.createchhub.shared.error.InvalidRequestException;
import br.com.mutua.createchhub.shared.error.ResourceConflictException;
import br.com.mutua.createchhub.shared.error.ResourceNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class StationLayoutService {

  private final SpaceRepository spaces;

  public StationLayoutService(SpaceRepository spaces) {
    this.spaces = spaces;
  }

  public SpacePageResponse list(UUID roomId, boolean publicView, int page, int size) {
    var room = spaces.findById(roomId).orElseThrow(this::roomNotFound);
    if (room.getType() != SpaceType.ROOM || (publicView && !room.isActive())) {
      throw roomNotFound();
    }
    var pageable = PageRequest.of(page, size, Sort.by("stationNumber", "id"));
    var result =
        publicView
            ? spaces.findByRoomIdAndActiveTrue(roomId, pageable)
            : spaces.findByRoomId(roomId, pageable);
    return SpacePageResponse.from(result.map(SpaceResponse::from));
  }

  @Transactional
  public SpaceResponse place(UUID id, StationPlacementRequest request) {
    var locked = spaces.findAllForUpdate(List.of(id, request.roomId()));
    var station =
        locked.stream()
            .filter(s -> s.getId().equals(id))
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("Estação não encontrada."));
    var room =
        locked.stream()
            .filter(s -> s.getId().equals(request.roomId()))
            .findFirst()
            .orElseThrow(this::roomNotFound);
    if (room.getType() != SpaceType.ROOM || room.getBookingMode() != BookingMode.PER_STATION) {
      throw new ResourceConflictException("O destino deve ser uma sala com reserva por estação.");
    }
    if (spaces.existsByRoomIdAndStationNumberAndIdNot(room.getId(), request.number(), id)) {
      throw new ResourceConflictException("Já existe uma estação com esse número nesta sala.");
    }
    station.placeStation(room.getId(), request.number(), request.x(), request.y());
    return SpaceResponse.from(spaces.saveAndFlush(station));
  }

  @Transactional
  public List<SpaceResponse> generate(UUID roomId, StationBatchRequest request) {
    var room = spaces.findForUpdate(roomId).orElseThrow(this::roomNotFound);
    requireStationRoom(room);
    var lastNumber = spaces.findLastStationNumber(roomId);
    var firstNumber = lastNumber == null ? 1 : (long) lastNumber + 1;
    if (firstNumber + request.count() - 1 > Integer.MAX_VALUE) {
      throw new InvalidRequestException("A numeração das estações excedeu o limite permitido.");
    }
    var firstPage =
        spaces.findByRoomId(roomId, PageRequest.of(0, 100, Sort.by("stationNumber", "id")));
    long rows = Math.ceilDiv(firstPage.getTotalElements() + request.count(), request.columns());
    var occupied = new HashSet<Long>();
    var page = firstPage;
    while (true) {
      for (var station : page) {
        long column =
            Math.min(
                request.columns() - 1,
                (long) (station.getMapX().doubleValue() * request.columns() / 100));
        long row = Math.min(rows - 1, (long) (station.getMapY().doubleValue() * rows / 100));
        occupied.add(row * request.columns() + column);
      }
      if (!page.hasNext()) break;
      page = spaces.findByRoomId(roomId, page.nextPageable());
    }
    var created = new ArrayList<Space>();
    for (long cell = 0; created.size() < request.count(); cell++) {
      if (occupied.contains(cell)) continue;
      int number = (int) (firstNumber + created.size());
      var station = spaces.save(new Space("Estação " + number, SpaceType.WORKSTATION, 1, null));
      var x = percent(cell % request.columns(), request.columns());
      var y = percent(cell / request.columns(), rows);
      station.placeStation(roomId, number, x, y);
      created.add(station);
    }
    spaces.flush();
    return created.stream().map(SpaceResponse::from).toList();
  }

  @Transactional
  public List<SpaceResponse> savePositions(UUID roomId, StationPositionsRequest request) {
    var ids = request.positions().stream().map(StationPositionsRequest.Position::id).toList();
    if (new HashSet<>(ids).size() != ids.size()) {
      throw new InvalidRequestException("Cada estação deve aparecer apenas uma vez.");
    }
    var lockIds = new ArrayList<>(ids);
    lockIds.add(roomId);
    Map<UUID, Space> locked =
        spaces.findAllForUpdate(lockIds).stream()
            .collect(Collectors.toMap(Space::getId, Function.identity()));
    var room = locked.get(roomId);
    if (room == null) throw roomNotFound();
    requireStationRoom(room);
    for (var position : request.positions()) {
      var station = locked.get(position.id());
      if (station == null) throw new ResourceNotFoundException("Estação não encontrada.");
      if (!roomId.equals(station.getRoomId())) {
        throw new ResourceConflictException("A estação não pertence a esta sala.");
      }
      if (station.getVersion() != position.version()) {
        throw new ResourceConflictException(
            "O mapa foi alterado por outra pessoa. Recarregue antes de salvar.");
      }
    }
    request.positions().forEach(p -> locked.get(p.id()).moveStation(p.x(), p.y()));
    spaces.flush();
    return request.positions().stream().map(p -> SpaceResponse.from(locked.get(p.id()))).toList();
  }

  private BigDecimal percent(long index, long count) {
    return BigDecimal.valueOf(index)
        .add(new BigDecimal("0.5"))
        .multiply(BigDecimal.valueOf(100))
        .divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
  }

  private void requireStationRoom(Space room) {
    if (room.getType() != SpaceType.ROOM || room.getBookingMode() != BookingMode.PER_STATION) {
      throw new ResourceConflictException(
          "Configure o espaço com reserva por estação antes de montar o mapa.");
    }
  }

  private ResourceNotFoundException roomNotFound() {
    return new ResourceNotFoundException("Sala não encontrada.");
  }
}
