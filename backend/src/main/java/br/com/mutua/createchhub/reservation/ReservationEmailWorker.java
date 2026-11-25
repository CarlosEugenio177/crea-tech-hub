package br.com.mutua.createchhub.reservation;

import br.com.mutua.createchhub.reservation.ReservationEmailQueue.DeliveryStatus;
import org.springframework.scheduling.annotation.Scheduled;

class ReservationEmailWorker {
  private final ReservationEmailQueue queue;
  private final ResendReservationSender sender;
  private final ReservationEmailLinks links;

  ReservationEmailWorker(
      ReservationEmailQueue queue, ResendReservationSender sender, ReservationEmailLinks links) {
    this.queue = queue;
    this.sender = sender;
    this.links = links;
  }

  @Scheduled(fixedDelay = 30_000, initialDelay = 30_000)
  void deliver() {
    queue.expireUnfinished();
    for (int processed = 0; processed < 8; processed++) {
      var job = queue.claim();
      if (job.isEmpty()) return;
      dispatch(job.get());
    }
  }

  private void dispatch(ReservationEmailQueue.Message message) {
    var token = links.token(message.reservationId());
    if (!queue.attachLink(message.reservationId(), ReservationTrackingToken.hash(token))) {
      queue.complete(message, DeliveryStatus.FAILED);
      return;
    }
    var status =
        switch (sender.send(message, links.url(token))) {
          case ACCEPTED -> DeliveryStatus.ACCEPTED;
          case RETRY -> message.attempt() >= 4 ? DeliveryStatus.FAILED : DeliveryStatus.QUEUED;
          case FAILED -> DeliveryStatus.FAILED;
        };
    queue.complete(message, status);
  }
}
