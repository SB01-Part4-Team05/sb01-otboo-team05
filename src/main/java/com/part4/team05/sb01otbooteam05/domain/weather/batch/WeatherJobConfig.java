package com.part4.team05.sb01otbooteam05.domain.weather.batch;

import com.part4.team05.sb01otbooteam05.domain.weather.entity.Weather;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class WeatherJobConfig {

  private final JobRepository jobRepository;
  private final PlatformTransactionManager platformTransactionManager;

  private final WeatherItemReader reader;
  private final WeatherItemProcessor processor;
  private final WeatherItemWriter writer;
  private final SingleLocationWeatherItemReader singleLocationWeatherItemReader;

  // 날씨 데이터 정기 배치
  @Bean
  public Job weatherJob() {
    return new JobBuilder("weatherJob", jobRepository)
        .start(weatherStep())
        .build();
  }

  @Bean
  public Step weatherStep() {
    return new StepBuilder("weatherStep", jobRepository)
        .<Pair<Integer, Integer>, List<Weather>>chunk(20, platformTransactionManager)
        .reader(reader)
        .processor(processor)
        .writer(writer)
        .build();
  }

  // 날씨 데이터 단건 배치
  @Bean
  public Job singleLocationWeatherJob() {
    return new JobBuilder("singleLocationWeatherJob", jobRepository)
        .start(singelLocationWeatherStep())
        .build();
  }

  @Bean
  public Step singelLocationWeatherStep() {
    return new StepBuilder("singleLocationWeatherStep", jobRepository)
        .<Pair<Integer, Integer>, List<Weather>>chunk(5, platformTransactionManager)
        .reader(singleLocationWeatherItemReader)
        .processor(processor)
        .writer(writer)
        .build();
  }
}
