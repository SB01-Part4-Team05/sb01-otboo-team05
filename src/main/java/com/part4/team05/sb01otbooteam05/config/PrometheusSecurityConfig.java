package com.part4.team05.sb01otbooteam05.config;

import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class PrometheusSecurityConfig {

  @Value("${ACTUATOR_USER:prometheus}")
  private String actuatorUser;

  @Value("${ACTUATOR_PASSWORD}")
  private String actuatorPassword;

  @Bean
  public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
    return new InMemoryUserDetailsManager(
        User.builder()
            .username(actuatorUser)
            .password(passwordEncoder.encode(actuatorPassword))
            .roles("MONITOR")
            .build()
    );
  }

  @Bean
  @Order(1)
  public SecurityFilterChain prometheusSecurityFilterChain(HttpSecurity http) throws Exception {
    return http
        .securityMatcher("/actuator/prometheus")
        .authorizeHttpRequests(auth -> auth
            .anyRequest().authenticated()
        )
        .httpBasic(withDefaults())
        .csrf(csrf -> csrf.disable())
        .build();
  }
}
