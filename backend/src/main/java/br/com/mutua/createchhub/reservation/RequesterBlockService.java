package br.com.mutua.createchhub.reservation;

import br.com.mutua.createchhub.shared.error.InvalidRequestException;
import br.com.mutua.createchhub.shared.error.ResourceConflictException;
import br.com.mutua.createchhub.shared.error.ResourceNotFoundException;
import br.com.mutua.createchhub.space.SpaceAvailabilityService;
import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
class RequesterBlockService {
  private static final DateTimeFormatter LOCAL_DATE_TIME =
      DateTimeFormatter.ofPattern("dd/MM/uuuu 'às' HH:mm")
          .withZone(SpaceAvailabilityService.HUB_ZONE);
  private static final LocalDate LAST_SUPPORTED_DATE = LocalDate.of(9999, 12, 31);

  private final ReservationRepository reservations;
  private final RequesterBlocks blocks;
  private final Clock clock;

  RequesterBlockService(ReservationRepository reservations, RequesterBlocks blocks, Clock clock) {
    this.reservations = reservations;
    this.blocks = blocks;
    this.clock = clock;
  }

  void checkSubmission(String cpf) {
    blocks.lock(cpf);
    blocks
        .activeUntil(cpf, clock.instant())
        .ifPresent(
            until -> {
              throw new ResponseStatusException(
                  HttpStatus.FORBIDDEN,
                  "Novas reservas estão bloqueadas por faltas. Você pode solicitar novamente em "
                      + LOCAL_DATE_TIME.format(until)
                      + ".");
            });
  }

  @Transactional
  public void block(UUID reservationId, UUID adminId, int days) {
    var cpf = reservations.requesterCpf(reservationId);
    blocks.lock(cpf);
    var reservation =
        reservations
            .findAdminStateForUpdate(reservationId)
            .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado."));
    if (!reservation.noShow() || blocks.absenceCount(cpf) < RequesterBlocks.MIN_ABSENCES) {
      throw new ResourceConflictException(
          "O bloqueio pode ser aplicado a partir da segunda falta registrada.");
    }
    var now = clock.instant();
    var until = now.plus(days, ChronoUnit.DAYS);
    if (until
        .atZone(SpaceAvailabilityService.HUB_ZONE)
        .toLocalDate()
        .isAfter(LAST_SUPPORTED_DATE)) {
      throw new InvalidRequestException("O prazo informado excede a data máxima suportada.");
    }
    blocks.replace(cpf, adminId, now, until);
  }

  @Transactional
  public void release(UUID reservationId, UUID adminId) {
    var cpf = reservations.requesterCpf(reservationId);
    blocks.lock(cpf);
    blocks.revoke(cpf, adminId, clock.instant());
  }
}
