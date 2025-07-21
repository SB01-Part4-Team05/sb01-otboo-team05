package com.part4.team05.sb01otbooteam05.config;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  @Value("${file.upload-dir:uploads}")
  private String uploadDir;

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    Path uploadPath = Paths.get(System.getProperty("user.dir"))
        .resolve(uploadDir)
        .toAbsolutePath()
        .normalize();

    registry.addResourceHandler("/uploads/**")
        .addResourceLocations(uploadPath.toUri().toString() + "/");
  }
}
