package com.mr486.mswebclient.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.web.client.RestTemplate;

@Configuration
public class GatewaySecurityConfiguration {
  @Bean
  RestTemplate restTemplate(@Value("${app.auth.username}") String u,
                            @Value("${app.auth.password}") String p) {
    RestTemplate rt = new RestTemplate();
    rt.getInterceptors().add(new BasicAuthenticationInterceptor(u, p));
    return rt;
  }

  @Bean
  ObjectMapper objectMapper() {
    return new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
  }
}
