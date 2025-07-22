package com.part4.team05.sb01otbooteam05.domain.weather.batch.listener;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomStepExecutionListener implements StepExecutionListener {

  private final MeterRegistry meterRegistry;

  @Override
  public ExitStatus afterStep(StepExecution stepExecution) {
    String stepName = stepExecution.getStepName();
    String status = stepExecution.getStatus().name();

    meterRegistry.counter(
        "batch.step.execution.count",
        "batch_step", stepName,
        "status", status
    ).increment();

    return stepExecution.getExitStatus();
  }
}
