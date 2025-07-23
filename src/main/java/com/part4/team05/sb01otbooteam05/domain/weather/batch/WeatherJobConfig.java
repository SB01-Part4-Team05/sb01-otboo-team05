package com.part4.team05.sb01otbooteam05.domain.weather.batch;


import com.part4.team05.sb01otbooteam05.domain.weather.batch.listener.CustomChunkListener;
import com.part4.team05.sb01otbooteam05.domain.weather.batch.listener.CustomItemProcessListener;
import com.part4.team05.sb01otbooteam05.domain.weather.batch.listener.CustomItemReadListener;
import com.part4.team05.sb01otbooteam05.domain.weather.batch.listener.CustomJobExecutionListener;
import com.part4.team05.sb01otbooteam05.domain.weather.batch.listener.CustomStepExecutionListener;
import com.part4.team05.sb01otbooteam05.domain.weather.batch.reader.OldWeatherItemReader;
import com.part4.team05.sb01otbooteam05.domain.weather.batch.reader.WeatherItemReader;
import com.part4.team05.sb01otbooteam05.domain.weather.batch.processor.WeatherItemProcessor;
import com.part4.team05.sb01otbooteam05.domain.weather.batch.writer.WeatherDeleteItemWriter;
import com.part4.team05.sb01otbooteam05.domain.weather.batch.writer.WeatherItemWriter;
import com.part4.team05.sb01otbooteam05.domain.weather.batch.writer.WeatherNotificationItemWriter;
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

  private final WeatherItemReader weatherItemReader;
  private final WeatherItemProcessor weatherItemProcessor;
  private final WeatherItemWriter weatherItemWriter;
  private final WeatherNotificationItemWriter weatherNotificationItemWriter;
  private final OldWeatherItemReader oldWeatherItemReader;
  private final WeatherDeleteItemWriter weatherDeleteItemWriter;
  private final CustomJobExecutionListener customJobExecutionListener;
  private final CustomStepExecutionListener customStepExecutionListener;
  private final CustomItemReadListener customItemReadListener;
  private final CustomItemProcessListener customItemProcessListener;
  private final CustomChunkListener customChunkListener;

  // 날씨 일괄 수집 작업 및 날씨 변동 알림을 정의하는 Spring Batch Job
  @Bean
  public Job weatherJob() {
    return new JobBuilder("weatherJob", jobRepository)
        .start(weatherStep())
        .next(weatherNotificationStep())
        .listener(customJobExecutionListener)
        .build();
  }

  // 날씨 일괄 수집 작업을 하는 Step
  @Bean
  public Step weatherStep() {
    return new StepBuilder("weatherStep", jobRepository)
        .<Pair<Integer, Integer>, List<Weather>>chunk(20, platformTransactionManager)
        .reader(weatherItemReader)
        .processor(weatherItemProcessor)
        .writer(weatherItemWriter)
        .listener(customStepExecutionListener)
        .listener(customChunkListener)
        .listener(customItemReadListener)
        .listener(customItemProcessListener)
        .build();
  }

  // 날씨 변동 알림 작업을 하는 Step
  @Bean
  public Step weatherNotificationStep() {
    return new StepBuilder("weatherNotificationStep", jobRepository)
        .<Pair<Integer, Integer>, Pair<Integer, Integer>>chunk(50, platformTransactionManager)
        .reader(weatherItemReader)
        .writer(weatherNotificationItemWriter)
        .listener(customStepExecutionListener)
        .listener(customChunkListener)
        .listener(customItemReadListener)
        .build();
  }

  // 날씨 데이터 삭제작업을 정의하는 Spring Batch Job
  @Bean
  public Job deleteOldWeatherJob() {
    return new JobBuilder("deleteOldWeatherJob", jobRepository)
        .start(deleteOldWeatherStep())
        .listener(customJobExecutionListener)
        .build();
  }

  @Bean
  public Step deleteOldWeatherStep() {
    return new StepBuilder("deleteOldWeatherStep", jobRepository)
        .<UUID, UUID>chunk(100, platformTransactionManager)
        .reader(oldWeatherItemReader)
        .writer(weatherDeleteItemWriter)
        .listener(customStepExecutionListener)
        .listener(customChunkListener)
        .listener(customItemReadListener)
        .build();
  }
}
