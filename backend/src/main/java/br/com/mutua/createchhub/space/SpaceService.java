package br.com.mutua.createchhub.space;

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
    return result.map(SpaceResponse::from);
  }

  public Page<PublicSpaceResponse> listPublic(int page, int size) {
    var pageable = PageRequest.of(page, size, Sort.by("name", "id"));
    return spaces.findByActive(true, pageable).map(PublicSpaceResponse::from);
  }

  public SpaceResponse find(UUID id) {
    return SpaceResponse.from(findSpace(id));
  }

  @Transactional
  public SpaceResponse create(SpaceRequest request) {
    var space =
        new Space(request.name(), request.type(), request.capacity(), request.description());
    return SpaceResponse.from(spaces.saveAndFlush(space));
  }

  @Transactional
  public SpaceResponse update(UUID id, SpaceRequest request) {
    var space = findSpace(id);
    space.update(request.name(), request.type(), request.capacity(), request.description());
    return SpaceResponse.from(space);
  }

  @Transactional
  public SpaceResponse changeStatus(UUID id, boolean active) {
    var space = findSpace(id);
    space.changeStatus(active);
    return SpaceResponse.from(space);
  }

  private Space findSpace(UUID id) {
    return spaces
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Espaço não encontrado."));
  }
}
