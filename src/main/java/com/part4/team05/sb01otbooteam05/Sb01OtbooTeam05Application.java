package com.part4.team05.sb01otbooteam05;

import com.part4.team05.sb01otbooteam05.domain.auth.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
@EnableJpaAuditing
@EnableScheduling
@EnableConfigurationProperties(JwtProperties.class)
public class Sb01OtbooTeam05Application {

	public static void main(String[] args) {
		SpringApplication.run(Sb01OtbooTeam05Application.class, args);
	}

}
