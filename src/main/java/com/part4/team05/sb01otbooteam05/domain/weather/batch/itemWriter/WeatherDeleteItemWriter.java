package com.part4.team05.sb01otbooteam05.domain.weather.batch.itemWriter;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@StepScope
@RequiredArgsConstructor
@Slf4j
public class WeatherDeleteItemWriter implements ItemWriter<UUID> {

  private final JdbcTemplate jdbcTemplate;

  int total = 0;

  //JDBC 기반 반복 삭제
  @Override
  public void write(Chunk<? extends UUID> items) {
    for (UUID id : items) {
      int result = jdbcTemplate.update("DELETE FROM weathers WHERE id = ?", id);
      total += result;
    }
    log.info("날씨 데이터 삭제 완료: 한 청크 내 총 {}건", total);
  }
}
