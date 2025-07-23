package com.part4.team05.sb01otbooteam05.domain.weather.batch.reader;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@StepScope
@RequiredArgsConstructor
public class OldWeatherItemReader implements ItemReader<UUID> {

  private final JdbcTemplate jdbcTemplate;

  @Value("#{jobParameters['deleteTime']}")
  private String deleteTimeStr;

  private Iterator<UUID> iterator;

  @BeforeStep
  public void init() {
    LocalDateTime delete = LocalDateTime.parse(deleteTimeStr);
    List<UUID> weatherIds = jdbcTemplate.query(
        "SELECT id FROM weathers WHERE forecasted_at <= ?",
        (rs, rowNum) -> UUID.fromString(rs.getString("id")),
        delete
    );
        this.iterator = weatherIds.iterator();
  }

  @Override
  public UUID read() {
    if(iterator != null && iterator.hasNext()) {
      return iterator.next();
    }
    return  null;
  }
}
