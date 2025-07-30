package com.part4.team05.sb01otbooteam05.config;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
  @GetMapping("/health")
  public String health() {
    return "OK";
  }
}
