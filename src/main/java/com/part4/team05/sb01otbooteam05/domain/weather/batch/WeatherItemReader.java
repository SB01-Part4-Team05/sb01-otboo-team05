package com.part4.team05.sb01otbooteam05.domain.weather.batch;

import com.part4.team05.sb01otbooteam05.domain.user.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;

@Component
@StepScope
@RequiredArgsConstructor
public class WeatherItemReader implements ItemReader<Pair<Integer, Integer>> {

  private final UserRepository userRepository;
  private Iterator<Pair<Integer, Integer>> iterator;

  @PostConstruct
  public void init() {
    this.iterator = userRepository.findDistinctLocations().stream()
        .map(arr -> Pair.of((Integer) arr[0], (Integer) arr[1]))
        .iterator();
  }

  @Override
  public Pair<Integer, Integer> read() {
    return iterator != null && iterator.hasNext() ? iterator.next() : null;
  }

}
