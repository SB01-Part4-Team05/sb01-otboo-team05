package com.part4.team05.sb01otbooteam05;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
// @EnableBatchProcessing
public class Sb01OtbooTeam05Application {

	public static void main(String[] args) {
		SpringApplication.run(Sb01OtbooTeam05Application.class, args);
	}

}
