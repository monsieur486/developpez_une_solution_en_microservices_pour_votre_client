package com.mr486.msrisque.configuration;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Configuration
public class FeignSecurityConfiguration {

  @Value("${security.app-user.username}")
  private String user;

  @Value("${security.app-user.password}")
  private String password;

  @Bean
  public RequestInterceptor resourceBAuthInterceptor() {
    return template -> {
      String token = user + ":" + password;
      String base64 = Base64.getEncoder().encodeToString(token.getBytes(StandardCharsets.UTF_8));
      template.header("Authorization", "Basic " + base64);
    };
  }
}
