package br.com.mutua.createchhub.block;

import br.com.mutua.createchhub.reservation.ReservationRepository;
import br.com.mutua.createchhub.shared.error.InvalidRequestException;
import br.com.mutua.createchhub.shared.error.ResourceConflictException;
import br.com.mutua.createchhub.shared.error.ResourceNotFoundException;
import br.com.mutua.createchhub.space.SpaceAvailabilityService;
import br.com.mutua.createchhub.space.SpaceRepository;
import jakarta.persistence.criteria.Predicate;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
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

  private static final DateTimeFormatter BRAZILIAN_DATE = DateTimeFormatter.ofPattern("dd/MM/uuuu");

  private final SpaceBlockRepository blocks;
  private final SpaceRepository spaces;
  private final ReservationRepository reservations;
  private final Clock clock;

  SpaceBlockService(
      SpaceBlockRepository blocks,
      SpaceRepository spaces,
      ReservationRepository reservations,
      Clock clock) {
    this.blocks = blocks;
    this.spaces = spaces;
    this.reservations = reservations;
    this.clock = clock;
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
    var start = LocalDateTime.parse(request.startsAt());
    var end = LocalDateTime.parse(request.endsAt());
    int count = occurrenceCount(start, end, request.repeatUntil());
    var seriesId = count == 1 ? null : UUID.randomUUID();
    var occurrences = new ArrayList<SpaceBlock>(count);
    for (int week = 0; week < count; week++) {
      var block =
          SpaceBlock.create(
              request.spaceId(),
              start.plusWeeks(week),
              end.plusWeeks(week),
              request.reason(),
              actor,
              seriesId);
      verifyNoConflict(block);
      occurrences.add(block);
    }
    return SpaceBlockResponse.from(blocks.saveAllAndFlush(occurrences).getFirst());
  }

  @Transactional
  public SpaceBlockResponse cancel(UUID id, UUID actor) {
    var block = blocks.findForUpdate(id).orElseThrow(this::notFound);
    block.cancel(actor);
    return SpaceBlockResponse.from(block);
  }

  @Transactional
  public void cancelSeries(UUID seriesId, UUID actor) {
    var occurrences = blocks.findSeriesForUpdate(seriesId);
    if (occurrences.isEmpty()) {
      throw new ResourceNotFoundException("Série de bloqueios não encontrada.");
    }
    var now = LocalDateTime.now(clock.withZone(SpaceAvailabilityService.HUB_ZONE));
    occurrences.stream()
        .filter(block -> block.getEndsAt().isAfter(now))
        .forEach(block -> block.cancel(actor));
  }

  private int occurrenceCount(LocalDateTime start, LocalDateTime end, String repeatUntil) {
    if (repeatUntil == null) return 1;
    LocalDate last;
    try {
      last = LocalDate.parse(repeatUntil);
    } catch (DateTimeParseException exception) {
      throw new InvalidRequestException("Informe uma data válida para encerrar a repetição.");
    }
    long weeks = ChronoUnit.WEEKS.between(start.toLocalDate(), last);
    if (weeks < 1 || weeks > 51) {
      throw new InvalidRequestException("A série deve ter de 2 a 52 ocorrências semanais.");
    }
    if (end.isAfter(start.plusWeeks(1)) || end.plusWeeks(weeks).getYear() > 9999) {
      throw new InvalidRequestException("Escolha um intervalo de até uma semana em datas válidas.");
    }
    return (int) weeks + 1;
  }

  private void verifyNoConflict(SpaceBlock block) {
    if (!reservations.hasConflictForBlock(
        block.getSpaceId(), block.getStartsAt(), block.getEndsAt())) {
      return;
    }
    var date =
        block.getSeriesId() == null
            ? "nesse horário"
            : "no bloqueio de " + BRAZILIAN_DATE.format(block.getStartsAt());
    throw new ResourceConflictException(
        "Já existe uma reserva pendente ou confirmada "
            + date
            + ". Confira a fila de reservas ou ajuste o intervalo.");
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
