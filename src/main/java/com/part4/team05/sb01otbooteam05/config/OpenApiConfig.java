package com.part4.team05.sb01otbooteam05.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI()
        .info(new Info()
            .title("고급프로젝트 5팀 옷장을 부탁해 API 문서")
            .description("스프린트 고급 프로젝트 옷장을 부탁해\n5팀의 Swagger API 문서입니다.")
            .version("1.0")
            .contact(new Contact()
                .name("5팀")
            ));
  }
}
