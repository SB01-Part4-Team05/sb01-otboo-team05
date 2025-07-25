package com.part4.team05.sb01otbooteam05.domain.weather.batch.writer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

  //JDBC 기반 반복 삭제
  @Override
  public void write(Chunk<? extends UUID> items) {

    List<Object[]> objects = new ArrayList<>();
    for (UUID id : items) {
      objects.add(new Object[]{id});
    }

    int[] result = jdbcTemplate.batchUpdate("DELETE FROM weathers WHERE id = ?", objects);

    int totalDeleted = Arrays.stream(result).sum();
    log.info("날씨 데이터 배치 삭제 완료: 총 {}건", totalDeleted);
  }
}
