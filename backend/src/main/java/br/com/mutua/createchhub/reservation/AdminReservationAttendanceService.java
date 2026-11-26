package br.com.mutua.createchhub.reservation;

import br.com.mutua.createchhub.shared.error.ResourceConflictException;
import br.com.mutua.createchhub.shared.error.ResourceNotFoundException;
import br.com.mutua.createchhub.space.SpaceAvailabilityService;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class AdminReservationAttendanceService {
  private final ReservationRepository reservations;
  private final Clock clock;
  private final RequesterBlocks blocks;

  AdminReservationAttendanceService(
      ReservationRepository reservations, Clock clock, RequesterBlocks blocks) {
    this.reservations = reservations;
    this.clock = clock;
    this.blocks = blocks;
  }

  @Transactional
  public void record(UUID id, UUID adminId, boolean noShow) {
    var cpf = reservations.requesterCpf(id);
    blocks.lock(cpf);
    var reservation =
        reservations
            .findAdminStateForUpdate(id)
            .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado."));
    var now = clock.instant();
    if (reservation.status() != ReservationStatus.CONFIRMED
        || LocalDateTime.ofInstant(now, SpaceAvailabilityService.HUB_ZONE)
            .isBefore(reservation.endsAt())) {
      throw new ResourceConflictException(
          "Registre falta somente após o término de uma reserva confirmada.");
    }

    if (reservation.noShow() != noShow) {
      reservations.recordNoShow(id, noShow ? adminId : null, noShow ? now : null);
      if (!noShow && blocks.absenceCount(cpf) < RequesterBlocks.MIN_ABSENCES) {
        blocks.revoke(cpf, adminId, now);
      }
    }
  }
}
