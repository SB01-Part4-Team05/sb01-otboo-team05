package com.part4.team05.sb01otbooteam05.domain.weather.batch.listener;

import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomJobExecutionListener implements JobExecutionListener {

  private final MeterRegistry meterRegistry;

  @Override
  public void afterJob(JobExecution jobExecution) {
    String jobName = jobExecution.getJobInstance().getJobName();
    String status = jobExecution.getStatus().name();

    meterRegistry.counter(
        "batch.job.execution.count",
        "batch_job", jobName,
        "status", status
    ).increment();

    if(jobExecution.getStartTime() != null && jobExecution.getEndTime() != null) {
      long duration = ChronoUnit.MILLIS.between(
          jobExecution.getStartTime(),
          jobExecution.getEndTime()
      );

    meterRegistry.timer(
        "batch.job.execution.time",
        "batch_job", jobName,
        "status", status
    ).record(duration, TimeUnit.MILLISECONDS);
    }
  }
}
