package br.com.mutua.createchhub.space;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.mutua.createchhub.shared.error.ResourceConflictException;
import br.com.mutua.createchhub.support.PostgresTestConfiguration;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(PostgresTestConfiguration.class)
class StationLayoutConcurrencyTests {

  @Autowired private SpaceRepository spaces;
  @Autowired private StationLayoutService layout;
  @Autowired private SpaceService service;
  @Autowired private RoomLayoutService roomLayout;

  @AfterEach
  void cleanup() {
    spaces.deleteAllInBatch();
  }

  @Test
  void onlyOneStationCanClaimTheSameNumberConcurrently() throws Exception {
    var room = spaces.saveAndFlush(new Space("Sala", SpaceType.ROOM, 2, null));
    service.changeBookingMode(room.getId(), BookingMode.PER_STATION);
    var left = spaces.saveAndFlush(new Space("A", SpaceType.WORKSTATION, 1, null));
    var right = spaces.saveAndFlush(new Space("B", SpaceType.WORKSTATION, 1, null));
    var start = new CountDownLatch(1);
    try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
      var a = executor.submit(() -> claim(start, left.getId(), room.getId()));
      var b = executor.submit(() -> claim(start, right.getId(), room.getId()));
      start.countDown();
      assertThat(a.get(10, TimeUnit.SECONDS)).isNotEqualTo(b.get(10, TimeUnit.SECONDS));
    }
    assertThat(layout.list(room.getId(), false, 0, 20).totalElements()).isEqualTo(1);
  }

  @Test
  void attachingAStationAndChangingItsBookingModeCannotBothSucceed() throws Exception {
    var room = spaces.saveAndFlush(new Space("Sala", SpaceType.ROOM, 2, null));
    service.changeBookingMode(room.getId(), BookingMode.PER_STATION);
    var station = spaces.saveAndFlush(new Space("Estação", SpaceType.WORKSTATION, 1, null));
    var start = new CountDownLatch(1);
    try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
      var attachment = executor.submit(() -> claim(start, station.getId(), room.getId()));
      var change =
          executor.submit(
              () -> {
                start.await();
                try {
                  service.changeBookingMode(room.getId(), BookingMode.WHOLE_SPACE);
                  return true;
                } catch (ResourceConflictException e) {
                  return false;
                }
              });
      start.countDown();
      assertThat(attachment.get(10, TimeUnit.SECONDS))
          .isNotEqualTo(change.get(10, TimeUnit.SECONDS));
    }
    var stored = spaces.findById(station.getId()).orElseThrow();
    assertThat(spaces.findById(room.getId()).orElseThrow().getBookingMode())
        .isEqualTo(stored.getRoomId() == null ? BookingMode.WHOLE_SPACE : BookingMode.PER_STATION);
  }

  @Test
  void removingAndMovingTheSameStationCannotBothSucceed() throws Exception {
    var room = spaces.saveAndFlush(new Space("Sala", SpaceType.ROOM, 2, null));
    var configured = service.changeBookingMode(room.getId(), BookingMode.PER_STATION);
    var station = layout.generate(room.getId(), new StationBatchRequest(1, 1)).getFirst();
    var movement =
        new RoomLayoutRequest(
            configured.version(),
            BigDecimal.TEN,
            BigDecimal.valueOf(6),
            java.util.List.of(
                new StationPositionsRequest.Position(
                    station.id(), station.version(), BigDecimal.TEN, BigDecimal.TEN)),
            java.util.List.of());
    var removal =
        new RoomLayoutRequest(
            configured.version(),
            BigDecimal.TEN,
            BigDecimal.valueOf(6),
            java.util.List.of(),
            java.util.List.of(
                new RoomLayoutRequest.RemovedStation(station.id(), station.version())));
    var start = new CountDownLatch(1);
    try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
      var a = executor.submit(() -> save(start, room.getId(), movement));
      var b = executor.submit(() -> save(start, room.getId(), removal));
      start.countDown();
      assertThat(a.get(10, TimeUnit.SECONDS)).isNotEqualTo(b.get(10, TimeUnit.SECONDS));
    }
    assertThat(spaces.findById(station.id())).isPresent();
  }

  private boolean save(CountDownLatch start, UUID id, RoomLayoutRequest request)
      throws InterruptedException {
    start.await();
    try {
      roomLayout.save(id, request);
      return true;
    } catch (ResourceConflictException e) {
      return false;
    }
  }

  private boolean claim(CountDownLatch start, UUID id, UUID roomId) throws InterruptedException {
    start.await();
    try {
      layout.place(id, new StationPlacementRequest(roomId, 1, BigDecimal.ZERO, BigDecimal.TEN));
      return true;
    } catch (ResourceConflictException e) {
      return false;
    }
  }
}
