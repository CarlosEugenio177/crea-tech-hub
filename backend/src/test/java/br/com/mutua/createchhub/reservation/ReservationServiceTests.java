package br.com.mutua.createchhub.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.mutua.createchhub.shared.error.ResourceConflictException;
import br.com.mutua.createchhub.space.SpaceAvailabilityService;
import br.com.mutua.createchhub.space.SpaceRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ReservationServiceTests {
  private static final String TOKEN = "a".repeat(43);
  private static final LocalDateTime STARTS_AT = LocalDateTime.of(2027, 3, 10, 14, 0);
  private static final LocalDateTime ENDS_AT = LocalDateTime.of(2027, 3, 10, 15, 0);

  @Test
  void treatsExact24HoursBeforeStartAsRegularCancellationAndOneSecondLaterAsLate() {
    var exact24h = STARTS_AT.minusHours(24).atZone(SpaceAvailabilityService.HUB_ZONE).toInstant();
    var repo = mock(ReservationRepository.class);
    var id = UUID.randomUUID();
    when(repo.findByTrackingHashForUpdate(any()))
        .thenReturn(
            Optional.of(
                new ReservationRepository.StoredReservation(
                    id,
                    "Sala",
                    null,
                    STARTS_AT,
                    ENDS_AT,
                    ReservationStatus.CONFIRMED,
                    null,
                    false,
                    null)));

    var atBoundary =
        new ReservationService(
            mock(SpaceRepository.class),
            mock(SpaceAvailabilityService.class),
            repo,
            Clock.fixed(exact24h, SpaceAvailabilityService.HUB_ZONE),
            mock(ReservationEmailQueue.class));
    var regularReceipt = atBoundary.cancel(TOKEN, new ReservationCancellationRequest(false));
    assertThat(regularReceipt.lateCancellation()).isFalse();
    verify(repo).markCancelled(id, exact24h, false);

    var repoLate = mock(ReservationRepository.class);
    when(repoLate.findByTrackingHashForUpdate(any()))
        .thenReturn(
            Optional.of(
                new ReservationRepository.StoredReservation(
                    id,
                    "Sala",
                    null,
                    STARTS_AT,
                    ENDS_AT,
                    ReservationStatus.CONFIRMED,
                    null,
                    false,
                    null)));
    var oneSecondLate = exact24h.plusSeconds(1);
    var afterBoundary =
        new ReservationService(
            mock(SpaceRepository.class),
            mock(SpaceAvailabilityService.class),
            repoLate,
            Clock.fixed(oneSecondLate, SpaceAvailabilityService.HUB_ZONE),
            mock(ReservationEmailQueue.class));

    assertThatThrownBy(() -> afterBoundary.cancel(TOKEN, new ReservationCancellationRequest(false)))
        .isInstanceOf(ResourceConflictException.class);
    verify(repoLate, never()).markCancelled(any(), any(), anyBoolean());

    var lateReceipt = afterBoundary.cancel(TOKEN, new ReservationCancellationRequest(true));
    assertThat(lateReceipt.lateCancellation()).isTrue();
    verify(repoLate).markCancelled(id, oneSecondLate, true);
  }

  @Test
  void allowsInProgressCancellationAsLateUntilEndTimeAndBlocksAtExactEndTime() {
    var id = UUID.randomUUID();
    var inProgress =
        STARTS_AT.plusMinutes(30).atZone(SpaceAvailabilityService.HUB_ZONE).toInstant();
    var repo = mock(ReservationRepository.class);
    when(repo.findByTrackingHashForUpdate(any()))
        .thenReturn(
            Optional.of(
                new ReservationRepository.StoredReservation(
                    id,
                    "Sala",
                    1,
                    STARTS_AT,
                    ENDS_AT,
                    ReservationStatus.CONFIRMED,
                    null,
                    false,
                    null)));
    var duringSlot =
        new ReservationService(
            mock(SpaceRepository.class),
            mock(SpaceAvailabilityService.class),
            repo,
            Clock.fixed(inProgress, SpaceAvailabilityService.HUB_ZONE),
            mock(ReservationEmailQueue.class));
    var cancelled = duringSlot.cancel(TOKEN, new ReservationCancellationRequest(true));
    assertThat(cancelled.status()).isEqualTo(ReservationStatus.CANCELLED);
    assertThat(cancelled.lateCancellation()).isTrue();

    var atEnd = ENDS_AT.atZone(SpaceAvailabilityService.HUB_ZONE).toInstant();
    var endedService =
        new ReservationService(
            mock(SpaceRepository.class),
            mock(SpaceAvailabilityService.class),
            repo,
            Clock.fixed(atEnd, SpaceAvailabilityService.HUB_ZONE),
            mock(ReservationEmailQueue.class));
    assertThatThrownBy(() -> endedService.cancel(TOKEN, new ReservationCancellationRequest(true)))
        .isInstanceOf(ResourceConflictException.class);
  }
}
