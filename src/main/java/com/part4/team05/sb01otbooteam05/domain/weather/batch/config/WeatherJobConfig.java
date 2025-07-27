package com.part4.team05.sb01otbooteam05.domain.weather.batch.config;


import com.part4.team05.sb01otbooteam05.domain.weather.batch.listener.CustomChunkListener;
import com.part4.team05.sb01otbooteam05.domain.weather.batch.listener.CustomItemProcessListener;
import com.part4.team05.sb01otbooteam05.domain.weather.batch.listener.CustomItemReadListener;
import com.part4.team05.sb01otbooteam05.domain.weather.batch.listener.CustomJobExecutionListener;
import com.part4.team05.sb01otbooteam05.domain.weather.batch.listener.CustomStepExecutionListener;
import com.part4.team05.sb01otbooteam05.domain.weather.batch.reader.WeatherItemReader;
import com.part4.team05.sb01otbooteam05.domain.weather.batch.processor.WeatherItemProcessor;
import com.part4.team05.sb01otbooteam05.domain.weather.batch.writer.WeatherItemWriter;
import com.part4.team05.sb01otbooteam05.domain.weather.batch.writer.WeatherNotificationItemWriter;
import com.part4.team05.sb01otbooteam05.domain.weather.entity.Weather;
import com.part4.team05.sb01otbooteam05.domain.weather.service.WeatherService;
import com.part4.team05.sb01otbooteam05.domain.weather.utils.BaseTimeUtils;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.integration.async.AsyncItemProcessor;
import org.springframework.batch.integration.async.AsyncItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class WeatherJobConfig {

  private final JobRepository jobRepository;
  private final PlatformTransactionManager platformTransactionManager;

  private final WeatherItemReader weatherItemReader;
  private final WeatherNotificationItemWriter weatherNotificationItemWriter;
  private final CustomJobExecutionListener customJobExecutionListener;
  private final CustomStepExecutionListener customStepExecutionListener;
  private final CustomItemReadListener customItemReadListener;
  private final CustomItemProcessListener customItemProcessListener;
  private final CustomChunkListener customChunkListener;

  // 날씨 일괄 수집 작업 및 날씨 변동 알림을 정의하는 Spring Batch Job
  @Bean
  public Job weatherJob(
      Step weatherStep,
      Step weatherNotificationStep
  ) {
    return new JobBuilder("weatherJob", jobRepository)
        .start(weatherStep)
        .next(weatherNotificationStep)
        .listener(customJobExecutionListener)
        .build();
  }

  // 날씨 일괄 수집 작업을 하는 Step
  @Bean
  public Step weatherStep(
      AsyncItemProcessor<Pair<Integer, Integer>, List<Weather>> asyncWeatherItemProcessor,
      AsyncItemWriter<List<Weather>> asyncWeatherItemWriter
  ) {
    return new StepBuilder("weatherStep", jobRepository)
        .<Pair<Integer, Integer>, Future<List<Weather>>>chunk(10, platformTransactionManager)
        .reader(weatherItemReader)
        .processor(asyncWeatherItemProcessor)
        .writer(asyncWeatherItemWriter)
        .listener(customStepExecutionListener)
        .listener(customChunkListener)
        .listener(customItemReadListener)
        .listener(customItemProcessListener)
        .faultTolerant()
        .skip(Exception.class)
        .skipLimit(1000) // 최대 1000건 예외 스킵
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

  // 비동기 TaskExecutor 세팅
  @Bean
  public TaskExecutor weatherTaskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(8);
    executor.setMaxPoolSize(16);
    executor.setQueueCapacity(100);
    executor.setThreadNamePrefix("weather-exec-");
    executor.setWaitForTasksToCompleteOnShutdown(true);
    executor.initialize();
    return executor;
  }

  // 비동기 ItemProcessor 세팅
  @Bean
  @StepScope
  public AsyncItemProcessor<Pair<Integer, Integer>, List<Weather>> asyncWeatherItemProcessor(
      WeatherItemProcessor weatherItemProcessor,
      WeatherService weatherService
  ) {
    LocalDate nowDate = LocalDate.now();
    LocalTime time = BaseTimeUtils.standardTime(LocalTime.now()).withNano(0);
    LocalDateTime forecastedAt = LocalDateTime.of(nowDate, time);

    Set<Pair<Integer, Integer>> existLocationSet = weatherService.findExistingWeatherLocations(forecastedAt);

    weatherItemProcessor.setExistLocationSet(existLocationSet);

    AsyncItemProcessor<Pair<Integer, Integer>, List<Weather>> processor = new AsyncItemProcessor<>();
    processor.setDelegate(weatherItemProcessor);
    processor.setTaskExecutor(weatherTaskExecutor());
    return processor;
  }

  // 비동기 ItemWriter 세팅
  @Bean
  @StepScope
  public AsyncItemWriter<List<Weather>> asyncWeatherItemWriter(
      WeatherItemWriter delegate
  ) {
    AsyncItemWriter<List<Weather>> writer = new AsyncItemWriter<>();
    writer.setDelegate(delegate);
    return writer;
  }

}
