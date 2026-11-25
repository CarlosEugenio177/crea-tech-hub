package br.com.mutua.createchhub.block;

import br.com.mutua.createchhub.reservation.ReservationRepository;
import br.com.mutua.createchhub.shared.error.InvalidRequestException;
import br.com.mutua.createchhub.shared.error.ResourceConflictException;
import br.com.mutua.createchhub.shared.error.ResourceNotFoundException;
import br.com.mutua.createchhub.space.SpaceRepository;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class SpaceBlockService {

  private final SpaceBlockRepository blocks;
  private final SpaceRepository spaces;
  private final ReservationRepository reservations;

  SpaceBlockService(
      SpaceBlockRepository blocks, SpaceRepository spaces, ReservationRepository reservations) {
    this.blocks = blocks;
    this.spaces = spaces;
    this.reservations = reservations;
  }

  public SpaceBlockPage list(
      UUID spaceId, Boolean active, String from, String to, int page, int size) {
    var start = parseFilter(from);
    var end = parseFilter(to);
    if (start != null && end != null && !end.isAfter(start)) {
      throw new InvalidRequestException("O fim da consulta deve ser posterior ao início.");
    }
    var scope = scope(spaceId);
    return SpaceBlockPage.from(
        blocks.findAll(
            filters(scope, active, start, end),
            PageRequest.of(page, size, Sort.by("startsAt", "id"))));
  }

  public SpaceBlockResponse find(UUID id) {
    return SpaceBlockResponse.from(blocks.findById(id).orElseThrow(this::notFound));
  }

  @Transactional
  public SpaceBlockResponse create(SpaceBlockRequest request, UUID actor) {
    if (spaces.findForUpdate(request.spaceId()).isEmpty()) {
      throw new ResourceNotFoundException("Espaço não encontrado.");
    }
    var block =
        SpaceBlock.create(
            request.spaceId(),
            LocalDateTime.parse(request.startsAt()),
            LocalDateTime.parse(request.endsAt()),
            request.reason(),
            actor);
    if (reservations.hasConflictForBlock(
        block.getSpaceId(), block.getStartsAt(), block.getEndsAt())) {
      throw new ResourceConflictException(
          "Já existe uma reserva pendente ou confirmada nesse horário. Confira a fila de reservas ou ajuste o intervalo.");
    }
    return SpaceBlockResponse.from(blocks.saveAndFlush(block));
  }

  @Transactional
  public SpaceBlockResponse cancel(UUID id, UUID actor) {
    var block = blocks.findForUpdate(id).orElseThrow(this::notFound);
    block.cancel(actor);
    return SpaceBlockResponse.from(block);
  }

  private List<UUID> scope(UUID spaceId) {
    if (spaceId == null) return null;
    var space =
        spaces
            .findById(spaceId)
            .orElseThrow(() -> new ResourceNotFoundException("Espaço não encontrado."));
    return space.getRoomId() == null ? List.of(spaceId) : List.of(spaceId, space.getRoomId());
  }

  private Specification<SpaceBlock> filters(
      List<UUID> scope, Boolean active, LocalDateTime from, LocalDateTime to) {
    return (root, query, builder) -> {
      var predicates = new ArrayList<Predicate>();
      if (scope != null) predicates.add(root.get("spaceId").in(scope));
      if (active != null)
        predicates.add(
            active
                ? builder.isNull(root.get("cancelledAt"))
                : builder.isNotNull(root.get("cancelledAt")));
      if (from != null) predicates.add(builder.greaterThan(root.get("endsAt"), from));
      if (to != null) predicates.add(builder.lessThan(root.get("startsAt"), to));
      return builder.and(predicates.toArray(Predicate[]::new));
    };
  }

  private LocalDateTime parseFilter(String value) {
    if (value == null) return null;
    try {
      return LocalDateTime.parse(value);
    } catch (DateTimeParseException exception) {
      throw new InvalidRequestException("Informe datas e horários válidos para a consulta.");
    }
  }

  private ResourceNotFoundException notFound() {
    return new ResourceNotFoundException("Bloqueio não encontrado.");
  }
}
