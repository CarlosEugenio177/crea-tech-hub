package br.com.mutua.createchhub.space;

import br.com.mutua.createchhub.shared.error.ResourceConflictException;
import br.com.mutua.createchhub.shared.error.ResourceNotFoundException;
import java.util.List;
import java.util.UUID;
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
    if (room.getType() != SpaceType.ROOM) {
      throw new ResourceConflictException("O ambiente de destino deve ser uma sala.");
    }
    if (spaces.existsByRoomIdAndStationNumberAndIdNot(room.getId(), request.number(), id)) {
      throw new ResourceConflictException("Já existe uma estação com esse número nesta sala.");
    }
    station.placeStation(room.getId(), request.number(), request.x(), request.y());
    return SpaceResponse.from(spaces.saveAndFlush(station));
  }

  private ResourceNotFoundException roomNotFound() {
    return new ResourceNotFoundException("Sala não encontrada.");
  }
}
