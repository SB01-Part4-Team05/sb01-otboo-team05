package com.part4.team05.sb01otbooteam05.domain.weather.scheduler;

import com.part4.team05.sb01otbooteam05.domain.weather.service.WeatherNotificationService;
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
public class WeatherScheduler {

  private final JobLauncher jobLauncher;
  private final Job weatherJob;
  private final Job deleteOldWeatherJob;
  private final WeatherNotificationService weatherNotificationService;

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

  @Scheduled(cron = "0 30 3 * * *")
  public void runDeleteOldWeatherJob() {
    LocalDateTime deleteTime = LocalDateTime.now().minusHours(24).withMinute(0).withSecond(0).withNano(0);
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

  @Scheduled(cron = "0 30 2,5,8,11,14,17,20,23 * * *")
  public void generateWeatherNotification() {
    try {
      weatherNotificationService.generateNotification();
      log.info("날씨 알림 생성 완료");
    } catch (Exception e) {
      log.error("날씨 알림 생성 중 오류", e);
    }
  }

}
