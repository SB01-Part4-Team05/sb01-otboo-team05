package com.part4.team05.sb01otbooteam05.domain.weather.batch.itemReader;

import com.part4.team05.sb01otbooteam05.domain.user.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import java.util.Iterator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;

@Component
@StepScope
@RequiredArgsConstructor
@Slf4j
public class WeatherItemReader implements ItemReader<Pair<Integer, Integer>> {

  private final UserRepository userRepository;
  private Iterator<Pair<Integer, Integer>> iterator;

  @PostConstruct
  public void init() {
    List<Object[]> locations = userRepository.findDistinctLocations();
    log.info("날씨 배치 대상 위치 수: {}", locations.size());

    this.iterator =locations.stream()
        .map(arr -> Pair.of((Integer) arr[0], (Integer) arr[1]))
        .iterator();
  }

  @Override
  public Pair<Integer, Integer> read() {
    if (iterator != null && iterator.hasNext()) {
      return iterator.next();
    }
    return null;
  }

}
