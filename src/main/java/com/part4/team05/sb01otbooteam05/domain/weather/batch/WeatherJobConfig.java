package com.part4.team05.sb01otbooteam05.domain.weather.batch;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class WeatherJobConfig {

  @Bean
  public Job weatherJob(JobRepository jobRepository, Step step) {
    return new JobBuilder("weatherJob", jobRepository)
        .start(step)
        .build();
  }

//  @Bean
//  public Step weatherStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
//    return new StepBuilder("weatherStep", jobRepository)
//        .
//  }

}
