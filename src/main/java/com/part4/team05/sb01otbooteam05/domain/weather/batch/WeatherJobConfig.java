package com.part4.team05.sb01otbooteam05.domain.weather.batch;

import com.part4.team05.sb01otbooteam05.domain.weather.batch.itemProcessor.WeatherItemProcessor;
import com.part4.team05.sb01otbooteam05.domain.weather.batch.itemReader.OldWeatherItemReader;
import com.part4.team05.sb01otbooteam05.domain.weather.batch.itemReader.SingleLocationWeatherItemReader;
import com.part4.team05.sb01otbooteam05.domain.weather.batch.itemReader.WeatherItemReader;
import com.part4.team05.sb01otbooteam05.domain.weather.batch.itemWriter.WeatherDeleteItemWriter;
import com.part4.team05.sb01otbooteam05.domain.weather.batch.itemWriter.WeatherItemWriter;
import com.part4.team05.sb01otbooteam05.domain.weather.entity.Weather;
import java.util.List;
import java.util.UUID;
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
  private final OldWeatherItemReader oldWeatherItemReader;
  private final WeatherDeleteItemWriter weatherDeleteItemWriter;


  // 날씨 일괄 수집 작업을 정의하는 Spring Batch Job
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

  // 날씨 단건 수집 작업을 정의하는 Spring Batch Job
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

  // 날씨 데이터 삭제작업을 정의하는 Spring Batch Job
  @Bean
  public Job deleteOldWeatherJob() {
    return new JobBuilder("deleteOldWeatherJob", jobRepository)
        .start(deleteOldWeatherStep())
        .build();
  }

  @Bean
  public Step deleteOldWeatherStep() {
    return new StepBuilder("deleteOldWeatherStep", jobRepository)
        .<UUID, UUID>chunk(100, platformTransactionManager)
        .reader(oldWeatherItemReader)
        .writer(weatherDeleteItemWriter)
        .build();
  }
}
