package br.com.mutua.createchhub.operatinghours;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.mutua.createchhub.support.PostgresTestConfiguration;
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
class OperatingHoursPersistenceTests {

  @Autowired private JdbcTemplate jdbc;

  @ParameterizedTest
  @CsvSource({"18:00,08:00", "08:00,08:00", "08:00:30,18:00", "08:00,24:00"})
  void databaseRejectsInvalidTimes(String opening, String closing) {
    assertThatThrownBy(
            () ->
                jdbc.update(
                    "UPDATE hub_operating_hours SET opening_time = CAST(? AS TIME), closing_time = CAST(? AS TIME) WHERE id = 1",
                    opening,
                    closing))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  void databaseRejectsUnknownDays() {
    assertThatThrownBy(
            () ->
                jdbc.update(
                    "INSERT INTO hub_operating_days (hub_id, day_of_week) VALUES (1, 'FUNDAY')"))
        .isInstanceOf(DataIntegrityViolationException.class);
  }
}
