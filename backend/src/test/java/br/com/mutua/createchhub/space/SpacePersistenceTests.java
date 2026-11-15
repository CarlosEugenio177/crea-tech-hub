package br.com.mutua.createchhub.space;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.mutua.createchhub.support.PostgresTestConfiguration;
import java.util.UUID;
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
class SpacePersistenceTests {

  @Autowired private JdbcTemplate jdbc;

  @ParameterizedTest
  @CsvSource({
    "Sala, UNKNOWN, 1, ck_spaces_type",
    "Sala, ROOM, 0, ck_spaces_capacity",
    "Estação, WORKSTATION, 2, ck_spaces_workstation_capacity",
    "'   ', ROOM, 1, ck_spaces_name"
  })
  void databaseRejectsInvalidSpacesEvenWithoutBeanValidation(
      String name, String type, int capacity, String constraint) {
    assertThatThrownBy(
            () ->
                jdbc.update(
                    "INSERT INTO spaces (id, name, type, capacity) VALUES (?, ?, ?, ?)",
                    UUID.randomUUID(),
                    name,
                    type,
                    capacity))
        .isInstanceOf(DataIntegrityViolationException.class)
        .hasMessageContaining(constraint);
  }
}
