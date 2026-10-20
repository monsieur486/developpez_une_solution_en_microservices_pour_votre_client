package com.mr486.mswebclient.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.web.client.RestTemplate;

@Configuration
public class GatewaySecurityConfiguration {
    @Bean
    public RestTemplate restTemplate(
            @Value("${app.auth.username}") String utilisateur,
            @Value("${app.auth.password}") String motDePasse) {
        RestTemplate client = new RestTemplate();
        client.getInterceptors().add(new BasicAuthenticationInterceptor(utilisateur, motDePasse));
        return client;
    }
}
