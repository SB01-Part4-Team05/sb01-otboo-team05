package com.part4.team05.sb01otbooteam05.domain.weather.batch.config;

import com.part4.team05.sb01otbooteam05.domain.weather.batch.listener.CustomChunkListener;
import com.part4.team05.sb01otbooteam05.domain.weather.batch.listener.CustomItemReadListener;
import com.part4.team05.sb01otbooteam05.domain.weather.batch.listener.CustomJobExecutionListener;
import com.part4.team05.sb01otbooteam05.domain.weather.batch.listener.CustomStepExecutionListener;
import com.part4.team05.sb01otbooteam05.domain.weather.batch.reader.OldWeatherItemReader;
import com.part4.team05.sb01otbooteam05.domain.weather.batch.writer.WeatherDeleteItemWriter;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
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
public class DeleteOldWeatherJobConfig {

  private final JobRepository jobRepository;
  private final PlatformTransactionManager platformTransactionManager;

  private final OldWeatherItemReader oldWeatherItemReader;
  private final WeatherDeleteItemWriter weatherDeleteItemWriter;
  private final CustomJobExecutionListener customJobExecutionListener;
  private final CustomStepExecutionListener customStepExecutionListener;
  private final CustomItemReadListener customItemReadListener;
  private final CustomChunkListener customChunkListener;

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

