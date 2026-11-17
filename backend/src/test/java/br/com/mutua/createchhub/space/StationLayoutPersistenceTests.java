package br.com.mutua.createchhub.space;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.mutua.createchhub.support.PostgresTestConfiguration;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Import(PostgresTestConfiguration.class)
@Transactional
class StationLayoutPersistenceTests {

  @Autowired private JdbcTemplate jdbc;
  private UUID roomId;
  private UUID stationId;

  @BeforeEach
  void prepare() {
    roomId = insert("ROOM");
    stationId = insert("WORKSTATION");
  }

  @ParameterizedTest
  @CsvSource({"0,25,40", "1,-1,40", "1,101,40", "1,25,-1", "1,25,101"})
  void rejectsInvalidNumbersAndCoordinates(int number, int x, int y) {
    assertThatThrownBy(() -> place(stationId, roomId, number, x, y))
        .isInstanceOf(DataIntegrityViolationException.class)
        .hasMessageContaining("ck_spaces_station_placement");
  }

  @Test
  void rejectsIncompletePlacement() {
    assertThatThrownBy(
            () -> jdbc.update("UPDATE spaces SET room_id = ? WHERE id = ?", roomId, stationId))
        .isInstanceOf(DataIntegrityViolationException.class)
        .hasMessageContaining("ck_spaces_station_placement");
  }

  @Test
  void rejectsDuplicateNumbersEvenWhenTheExistingStationIsInactive() {
    place(stationId, roomId, 1, 25, 40);
    jdbc.update("UPDATE spaces SET active = false WHERE id = ?", stationId);
    var other = insert("WORKSTATION");
    assertThatThrownBy(() -> place(other, roomId, 1, 50, 60))
        .isInstanceOf(DataIntegrityViolationException.class)
        .hasMessageContaining("uq_spaces_room_station_number");
  }

  @Test
  void rejectsAParentThatIsNotARoom() {
    var table = insert("SHARED_TABLE");
    assertThatThrownBy(() -> place(stationId, table, 1, 25, 40))
        .isInstanceOf(DataIntegrityViolationException.class)
        .hasMessageContaining("fk_spaces_station_room");
  }

  @Test
  void preventsChangingTheTypeOfARoomThatHasStations() {
    place(stationId, roomId, 1, 25, 40);
    assertThatThrownBy(
            () -> jdbc.update("UPDATE spaces SET type = 'SHARED_TABLE' WHERE id = ?", roomId))
        .isInstanceOf(DataIntegrityViolationException.class)
        .hasMessageContaining("fk_spaces_station_room");
  }

  private UUID insert(String type) {
    var id = UUID.randomUUID();
    jdbc.update(
        "INSERT INTO spaces (id, name, type, capacity) VALUES (?, 'Teste', ?, 1)", id, type);
    return id;
  }

  private void place(UUID id, UUID room, int number, int x, int y) {
    jdbc.update(
        "UPDATE spaces SET room_id = ?, station_number = ?, map_x = ?, map_y = ? WHERE id = ?",
        room,
        number,
        x,
        y,
        id);
  }
}
