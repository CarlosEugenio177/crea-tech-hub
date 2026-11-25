package br.com.mutua.createchhub.space;

import br.com.mutua.createchhub.shared.error.ResourceConflictException;
import br.com.mutua.createchhub.shared.error.ResourceNotFoundException;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class SpaceService {

  private final SpaceRepository spaces;

  public SpaceService(SpaceRepository spaces) {
    this.spaces = spaces;
  }

  public Page<SpaceResponse> list(Boolean active, int page, int size) {
    var pageable = PageRequest.of(page, size, Sort.by("name", "id"));
    var result = active == null ? spaces.findAll(pageable) : spaces.findByActive(active, pageable);
    return result.map(space -> SpaceResponse.from(space));
  }

  public Page<PublicSpaceResponse> listPublic(int page, int size) {
    var pageable = PageRequest.of(page, size, Sort.by("name", "id"));
    return spaces.findPublic(pageable).map(space -> PublicSpaceResponse.from(space));
  }

  public SpaceResponse find(UUID id) {
    return SpaceResponse.from(findSpace(id));
  }

  @Transactional
  public SpaceResponse create(SpaceRequest request) {
    var space =
        new Space(request.name(), request.type(), request.capacity(), request.description());
    space.changePhoto(request.photoUrl());
    return SpaceResponse.from(spaces.saveAndFlush(space));
  }

  @Transactional
  public SpaceResponse update(UUID id, SpaceRequest request) {
    var space = findForUpdate(id);
    if (space.getType() != request.type() && spaces.existsByRoomId(id)) {
      throw new ResourceConflictException(
          "Uma sala com estações vinculadas não pode mudar de tipo.");
    }
    space.update(request.name(), request.type(), request.capacity(), request.description());
    space.changePhoto(request.photoUrl());
    spaces.flush();
    return SpaceResponse.from(space);
  }

  @Transactional
  public SpaceResponse changeStatus(UUID id, boolean active) {
    var space = findForUpdate(id);
    space.changeStatus(active);
    spaces.flush();
    return SpaceResponse.from(space);
  }

  @Transactional
  public SpaceResponse changeBookingMode(UUID id, BookingMode mode) {
    var space = findForUpdate(id);
    if (mode == BookingMode.WHOLE_SPACE && spaces.existsByRoomId(id)) {
      throw new ResourceConflictException(
          "Um espaço com estações vinculadas deve permanecer no modo por estação.");
    }
    space.changeBookingMode(mode);
    spaces.flush();
    return SpaceResponse.from(space);
  }

  private Space findSpace(UUID id) {
    return spaces
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Espaço não encontrado."));
  }

  private Space findForUpdate(UUID id) {
    return spaces
        .findForUpdate(id)
        .orElseThrow(() -> new ResourceNotFoundException("Espaço não encontrado."));
  }
}
