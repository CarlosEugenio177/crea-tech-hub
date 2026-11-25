package br.com.mutua.createchhub.block;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.mutua.createchhub.space.Space;
import br.com.mutua.createchhub.space.SpaceRepository;
import br.com.mutua.createchhub.space.SpaceType;
import br.com.mutua.createchhub.support.PostgresTestConfiguration;
import br.com.mutua.createchhub.user.AccessRole;
import br.com.mutua.createchhub.user.UserAccount;
import br.com.mutua.createchhub.user.UserAccountRepository;
import java.util.Set;
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
class SpaceBlockConcurrencyTests {

  @Autowired private SpaceBlockService service;
  @Autowired private SpaceBlockRepository blocks;
  @Autowired private SpaceRepository spaces;
  @Autowired private UserAccountRepository accounts;

  @AfterEach
  void cleanup() {
    blocks.deleteAllInBatch();
    spaces.deleteAllInBatch();
    accounts.deleteAll();
  }

  @Test
  void concurrentCancellationsKeepTheFirstActorAndTimestamp() throws Exception {
    var room = spaces.saveAndFlush(new Space("Sala", SpaceType.ROOM, 4, null));
    var first =
        accounts.saveAndFlush(
            new UserAccount("Primeiro", "first@example.com", "test-hash", AccessRole.ADMIN));
    var second =
        accounts.saveAndFlush(
            new UserAccount("Segundo", "second@example.com", "test-hash", AccessRole.ADMIN));
    var block =
        service.create(
            new SpaceBlockRequest(
                room.getId(), "2027-03-10T09:00", "2027-03-10T10:00", "Reunião", null),
            first.getId());
    var start = new CountDownLatch(1);
    try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
      var left =
          executor.submit(
              () -> {
                start.await();
                return service.cancel(block.id(), first.getId());
              });
      var right =
          executor.submit(
              () -> {
                start.await();
                return service.cancel(block.id(), second.getId());
              });
      start.countDown();
      var result = left.get(10, TimeUnit.SECONDS);
      assertThat(right.get(10, TimeUnit.SECONDS)).isEqualTo(result);
      assertThat(service.find(block.id())).isEqualTo(result);
      assertThat(result.cancelledBy()).isIn(Set.of(first.getId(), second.getId()));
      assertThat(result.active()).isFalse();
    }
  }
}
