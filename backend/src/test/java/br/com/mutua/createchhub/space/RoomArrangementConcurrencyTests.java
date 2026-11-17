package br.com.mutua.createchhub.space;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.mutua.createchhub.shared.error.ResourceConflictException;
import br.com.mutua.createchhub.support.PostgresTestConfiguration;
import java.math.BigDecimal;
import java.util.List;
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
class RoomArrangementConcurrencyTests {
  @Autowired private SpaceRepository spaces;
  @Autowired private SpaceService service;
  @Autowired private StationLayoutService layout;

  @AfterEach
  void cleanup() {
    spaces.deleteAllInBatch();
  }

  @Test
  void concurrentGenerationKeepsUniqueNumbers() throws Exception {
    var room = room();
    var start = new CountDownLatch(1);
    try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
      var left =
          executor.submit(
              () -> {
                start.await();
                return layout.generate(room, new StationBatchRequest(4, 2));
              });
      var right =
          executor.submit(
              () -> {
                start.await();
                return layout.generate(room, new StationBatchRequest(4, 2));
              });
      start.countDown();
      assertThat(left.get(10, TimeUnit.SECONDS)).hasSize(4);
      assertThat(right.get(10, TimeUnit.SECONDS)).hasSize(4);
    }
    assertThat(layout.list(room, false, 0, 100).items())
        .extracting(s -> s.stationPlacement().number())
        .containsExactly(1, 2, 3, 4, 5, 6, 7, 8);
  }

  @Test
  void rejectsOneOfTwoConcurrentEditsBasedOnTheSameVersion() throws Exception {
    var room = room();
    var station = layout.generate(room, new StationBatchRequest(1, 1)).getFirst();
    var start = new CountDownLatch(1);
    try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
      var left = executor.submit(() -> edit(start, room, station, 10));
      var right = executor.submit(() -> edit(start, room, station, 90));
      start.countDown();
      assertThat(left.get(10, TimeUnit.SECONDS)).isNotEqualTo(right.get(10, TimeUnit.SECONDS));
    }
    assertThat(service.find(station.id()).stationPlacement().x())
        .isIn(new BigDecimal("10.00"), new BigDecimal("90.00"));
  }

  private boolean edit(CountDownLatch start, UUID room, SpaceResponse station, int x)
      throws InterruptedException {
    start.await();
    try {
      layout.savePositions(
          room,
          new StationPositionsRequest(
              List.of(
                  new StationPositionsRequest.Position(
                      station.id(), station.version(), BigDecimal.valueOf(x), BigDecimal.TEN))));
      return true;
    } catch (ResourceConflictException exception) {
      return false;
    }
  }

  private UUID room() {
    var room = spaces.saveAndFlush(new Space("Sala", SpaceType.ROOM, 16, null));
    service.changeBookingMode(room.getId(), BookingMode.PER_STATION);
    return room.getId();
  }
}
