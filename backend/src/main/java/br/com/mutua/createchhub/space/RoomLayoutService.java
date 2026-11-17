package br.com.mutua.createchhub.space;

import br.com.mutua.createchhub.shared.error.InvalidRequestException;
import br.com.mutua.createchhub.shared.error.ResourceConflictException;
import br.com.mutua.createchhub.shared.error.ResourceNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoomLayoutService {

  private final SpaceRepository spaces;

  public RoomLayoutService(SpaceRepository spaces) {
    this.spaces = spaces;
  }

  @Transactional
  public SpaceResponse save(UUID roomId, RoomLayoutRequest request) {
    var ids = new ArrayList<UUID>();
    request.positions().forEach(p -> ids.add(p.id()));
    request.removedStations().forEach(p -> ids.add(p.id()));
    if (new HashSet<>(ids).size() != ids.size()) {
      throw new InvalidRequestException("Cada estação deve aparecer apenas uma vez na edição.");
    }
    ids.add(roomId);
    var locked =
        spaces.findAllForUpdate(ids).stream()
            .collect(Collectors.toMap(Space::getId, Function.identity()));
    var room = locked.get(roomId);
    if (room == null) throw new ResourceNotFoundException("Sala não encontrada.");
    if (room.getType() != SpaceType.ROOM || room.getBookingMode() != BookingMode.PER_STATION) {
      throw new ResourceConflictException(
          "Configure a sala com reserva por estação antes de editar o mapa.");
    }
    requireVersion(room, request.version());
    request.positions().forEach(p -> requireStation(locked.get(p.id()), roomId, p.version()));
    request.removedStations().forEach(p -> requireStation(locked.get(p.id()), roomId, p.version()));
    room.resizeRoom(request.width(), request.length());
    request.positions().forEach(p -> locked.get(p.id()).moveStation(p.x(), p.y()));
    request.removedStations().forEach(p -> locked.get(p.id()).removeFromRoom());
    spaces.flush();
    return SpaceResponse.from(room);
  }

  private void requireStation(Space station, UUID roomId, long version) {
    if (station == null) throw new ResourceNotFoundException("Estação não encontrada.");
    if (!roomId.equals(station.getRoomId())) {
      throw new ResourceConflictException("A estação não pertence a esta sala.");
    }
    requireVersion(station, version);
  }

  private void requireVersion(Space space, long version) {
    if (space.getVersion() != version) {
      throw new ResourceConflictException(
          "O mapa foi alterado por outra pessoa. Recarregue antes de salvar.");
    }
  }
}
