package com.part4.team05.sb01otbooteam05.domain.weather.batch.writer;

import com.part4.team05.sb01otbooteam05.domain.weather.service.WeatherNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@StepScope
@RequiredArgsConstructor
public class WeatherNotificationItemWriter implements ItemWriter<Pair<Integer, Integer>> {

  private final WeatherNotificationService weatherNotificationService;

  @Override
  public void write(Chunk<? extends Pair<Integer, Integer>> items) {
    for (Pair<Integer, Integer> loc : items) {
      try {
        weatherNotificationService.generateNotifications(loc.getLeft(), loc.getRight());
      } catch (Exception e) {
        log.error("알림 처리 중 오류 발생: x={}, y={}, err={}", loc.getLeft(), loc.getRight(),
            e.getMessage(), e);
      }
    }
  }
}
