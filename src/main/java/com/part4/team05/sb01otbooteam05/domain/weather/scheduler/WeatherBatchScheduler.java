package com.part4.team05.sb01otbooteam05.domain.weather.scheduler;

import java.time.LocalDateTime;
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
  private final Job deleteOldWeatherJob;

  @Scheduled(cron = "0 5 2,5,8,11,14,17,20,23 * * *")
  public void runWeatherJob() {
    try {
      JobParameters parameters = new JobParametersBuilder()
          .addLong("timestamp", System.currentTimeMillis())
          .toJobParameters();

      jobLauncher.run(weatherJob, parameters);
    } catch (Exception e) {
      log.error("날씨 3시간 간격 정기 배치 실행 중 오류 발생", e);
    }
  }

  @Scheduled(cron = "0 15 2 * * *")
  public void runDeleteOldWeatherJob() {
    LocalDateTime deleteTime = LocalDateTime.now().minusHours(25).withMinute(0).withSecond(0).withNano(0);
    try {
      JobParameters parameters = new JobParametersBuilder()
          .addString("deleteTime", deleteTime.toString())
          .addLong("timestamp", System.currentTimeMillis())
          .toJobParameters();

      jobLauncher.run(deleteOldWeatherJob, parameters);
    } catch (Exception e) {
      log.error("날씨 정기 삭제 배치 실행 중 오류 발생", e);
    }
  }
}
