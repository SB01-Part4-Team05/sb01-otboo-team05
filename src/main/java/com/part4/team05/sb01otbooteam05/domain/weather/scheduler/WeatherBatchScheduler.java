package com.part4.team05.sb01otbooteam05.domain.weather.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class WeatherBatchScheduler {

  private final JobLauncher jobLauncher;
  private final Job weatherJob;

//  @Scheduled(cron = "0 5 2,5,8,11,14,17,20,23 * * *")
  @Scheduled(cron = "0 */5 * * * *")
  public void batchWeatherForAllLocations() {
    try {
      JobParameters parameters = new JobParametersBuilder()
          .addLong("timestamp", System.currentTimeMillis())
          .toJobParameters();

      jobLauncher.run(weatherJob, parameters);
    } catch (Exception e) {
      log.error("날씨 3시간 간격 정기 배치 실행 중 오류 발생", e);
    }
  }
}
