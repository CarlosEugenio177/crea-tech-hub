package br.com.mutua.createchhub.reservation;

import br.com.mutua.createchhub.shared.error.ResourceConflictException;
import br.com.mutua.createchhub.shared.error.ResourceNotFoundException;
import br.com.mutua.createchhub.space.BookingMode;
import br.com.mutua.createchhub.space.Space;
import br.com.mutua.createchhub.space.SpaceAvailability;
import br.com.mutua.createchhub.space.SpaceAvailabilityService;
import br.com.mutua.createchhub.space.SpaceRepository;
import io.swagger.v3.oas.annotations.media.Schema;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReservationService {
  private final SpaceRepository spaces;
  private final SpaceAvailabilityService availability;
  private final ReservationRepository reservations;
  private final Clock clock;
  private final SecureRandom random = new SecureRandom();

  public ReservationService(
      SpaceRepository spaces,
      SpaceAvailabilityService availability,
      ReservationRepository reservations,
      Clock clock) {
    this.spaces = spaces;
    this.availability = availability;
    this.reservations = reservations;
    this.clock = clock;
  }

  @Transactional
  public ReservationSubmission submit(ReservationRequest request) {
    var scope = lockScope(request);
    var result = availability.check(scope.selected().getId(), request.startsAt(), request.endsAt());
    boolean free =
        scope.requestedStation() == null
            ? result.status() == SpaceAvailability.Status.AVAILABLE
            : result.stations().stream()
                .anyMatch(
                    station ->
                        station.id().equals(scope.requestedStation().getId())
                            && station.available());
    if (!free)
      throw new ResourceConflictException("Esse horário não está mais livre. Escolha outra opção.");

    var resource = scope.requestedStation() == null ? scope.selected() : scope.requestedStation();
    var now = clock.instant();
    var nowLocal = LocalDateTime.ofInstant(now, SpaceAvailabilityService.HUB_ZONE);
    var startsAt = LocalDateTime.parse(request.startsAt());
    var endsAt = LocalDateTime.parse(request.endsAt());
    boolean cancellable = nowLocal.isBefore(endsAt);
    var receipt =
        new ReservationReceipt(
            UUID.randomUUID(),
            scope.parent() == null ? resource.getName() : scope.parent().getName(),
            resource.getStationNumber(),
            startsAt,
            endsAt,
            ReservationStatus.PENDING,
            cancellable,
            cancellable && isLate(startsAt, nowLocal),
            false,
            null,
            null);
    byte[] secret = new byte[32];
    random.nextBytes(secret);
    var token = Base64.getUrlEncoder().withoutPadding().encodeToString(secret);
    reservations.persistPending(
        receipt, resource.getId(), resource.getRoomId(), request.requester(), digest(token), now);
    return new ReservationSubmission(receipt, token);
  }

  @Transactional(readOnly = true)
  public ReservationReceipt receipt(String token) {
    var nowLocal = LocalDateTime.now(clock.withZone(SpaceAvailabilityService.HUB_ZONE));
    return reservations
        .findByTrackingHash(digest(token))
        .map(row -> toReceipt(row, nowLocal))
        .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado."));
  }

  @Transactional
  public ReservationReceipt cancel(String token, ReservationCancellationRequest request) {
    var row =
        reservations
            .findByTrackingHashForUpdate(digest(token))
            .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado."));
    var now = clock.instant().truncatedTo(ChronoUnit.MICROS);
    var nowLocal = LocalDateTime.ofInstant(now, SpaceAvailabilityService.HUB_ZONE);
    if (row.status() == ReservationStatus.CANCELLED) {
      return toReceipt(row, nowLocal);
    }
    if (row.status() == ReservationStatus.REJECTED) {
      throw new ResourceConflictException(
          "Esta solicitação foi recusada e não pode ser cancelada.");
    }
    if (!nowLocal.isBefore(row.endsAt())) {
      throw new ResourceConflictException(
          "O horário desta reserva já encerrou e não pode mais ser cancelado.");
    }
    boolean late = isLate(row.startsAt(), nowLocal);
    if (late && !Boolean.TRUE.equals(request.confirmLateCancellation())) {
      throw new ResourceConflictException(
          "Faltam menos de 24 horas para o início da reserva. Confirme o cancelamento tardio para continuar.");
    }
    reservations.markCancelled(row.id(), now, late);
    return toReceipt(
        new ReservationRepository.StoredReservation(
            row.id(),
            row.spaceName(),
            row.stationNumber(),
            row.startsAt(),
            row.endsAt(),
            ReservationStatus.CANCELLED,
            now,
            late,
            row.decisionNote()),
        nowLocal);
  }

  private static boolean isLate(LocalDateTime startsAt, LocalDateTime nowLocal) {
    return nowLocal.isAfter(startsAt.minusHours(24));
  }

  private static ReservationReceipt toReceipt(
      ReservationRepository.StoredReservation row, LocalDateTime nowLocal) {
    boolean cancellable =
        (row.status() == ReservationStatus.PENDING || row.status() == ReservationStatus.CONFIRMED)
            && nowLocal.isBefore(row.endsAt());
    return new ReservationReceipt(
        row.id(),
        row.spaceName(),
        row.stationNumber(),
        row.startsAt(),
        row.endsAt(),
        row.status(),
        cancellable,
        cancellable && isLate(row.startsAt(), nowLocal),
        row.lateCancellation(),
        row.cancelledAt(),
        row.decisionNote());
  }

  private Scope lockScope(ReservationRequest request) {
    var selectedId = request.spaceId();
    var parentId = spaces.findParentId(selectedId);
    var ids = new HashSet<UUID>();
    ids.add(selectedId);
    if (parentId != null) ids.add(parentId);
    if (request.stationId() != null) ids.add(request.stationId());
    var locked = spaces.findAllForUpdate(List.copyOf(ids));
    var selected =
        locked.stream()
            .filter(space -> space.getId().equals(selectedId))
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("Espaço não encontrado."));
    var parent = selected.getRoomId() == null ? null : required(locked, selected.getRoomId());
    if (selected.getBookingMode() == BookingMode.PER_STATION && request.stationId() == null) {
      throw new ResourceConflictException("Escolha uma estação dessa sala.");
    }
    if (request.stationId() == null) return new Scope(selected, null, parent);
    var station = required(locked, request.stationId());
    if (selected.getBookingMode() != BookingMode.PER_STATION
        || !selectedId.equals(station.getRoomId())) {
      throw new ResourceConflictException("Escolha uma estação dessa sala.");
    }
    return new Scope(selected, station, selected);
  }

  private Space required(List<Space> locked, UUID id) {
    return locked.stream()
        .filter(space -> space.getId().equals(id))
        .findFirst()
        .orElseThrow(() -> new ResourceConflictException("O espaço mudou. Escolha outra opção."));
  }

  private static String digest(String token) {
    try {
      return HexFormat.of()
          .formatHex(
              MessageDigest.getInstance("SHA-256")
                  .digest(token.getBytes(StandardCharsets.US_ASCII)));
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException("SHA-256 indisponível.", exception);
    }
  }

  private record Scope(Space selected, Space requestedStation, Space parent) {}

  @Schema(requiredProperties = {"receipt", "trackingToken"})
  public record ReservationSubmission(ReservationReceipt receipt, String trackingToken) {}
}
