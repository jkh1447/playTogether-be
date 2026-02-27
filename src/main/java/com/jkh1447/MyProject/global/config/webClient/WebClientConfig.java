package com.jkh1447.MyProject.global.config.webClient;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class WebClientConfig {
  
  @Bean
  public WebClient geminiWebClient(WebClient.Builder builder, @Value("${gemini.api.baseUrl}") String baseUrl) {
    return builder
      .baseUrl(baseUrl)
      .filter((request, next) -> {
          // 실제로 요청되는 URL을 로그에 찍어줍니다.
          log.info("Gemini Request URL: {}", request.url());
          return next.exchange(request);
      })
      .defaultHeader("Content-Type", "application/json")
      .build();
  }
}
