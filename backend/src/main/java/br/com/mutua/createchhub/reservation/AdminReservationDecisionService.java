package br.com.mutua.createchhub.reservation;

import br.com.mutua.createchhub.shared.error.ResourceConflictException;
import br.com.mutua.createchhub.shared.error.ResourceNotFoundException;
import java.time.Clock;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminReservationDecisionService {
  private final ReservationRepository reservations;
  private final Clock clock;
  private final ReservationEmailQueue emails;

  AdminReservationDecisionService(
      ReservationRepository reservations, Clock clock, ReservationEmailQueue emails) {
    this.reservations = reservations;
    this.clock = clock;
    this.emails = emails;
  }

  @Transactional
  public void decide(UUID id, UUID adminId, AdminReservationDecisionRequest request) {
    var reservation =
        reservations
            .findAdminStateForUpdate(id)
            .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado."));
    var current = reservation.status();
    if (current != request.expectedStatus()) {
      throw new ResourceConflictException(
          "O pedido foi atualizado. Confira a situação antes de decidir.");
    }

    var next =
        switch (request.action()) {
          case CONFIRM -> ReservationStatus.CONFIRMED;
          case REJECT -> ReservationStatus.REJECTED;
          case CANCEL -> ReservationStatus.CANCELLED;
        };
    if (current != ReservationStatus.PENDING
        && !(current == ReservationStatus.CONFIRMED && next == ReservationStatus.CANCELLED)) {
      throw new ResourceConflictException(
          "Essa ação não está disponível para a situação do pedido.");
    }

    if (reservation.noShow()) {
      throw new ResourceConflictException(
          "Desfaça a falta registrada antes de cancelar a reserva.");
    }

    var note = request.note() == null || request.note().isBlank() ? null : request.note().strip();
    reservations.recordDecision(id, adminId, next, note, clock.instant());
    emails.enqueue(id, next);
  }
}
