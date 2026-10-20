package com.mr486.mswebclient.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class GatewaySecurityConfiguration {
    @Bean
    public RestTemplate restTemplate(
            RestTemplateBuilder constructeur,
            @Value("${app.auth.username}") String utilisateur,
            @Value("${app.auth.password}") String motDePasse) {
        return constructeur.basicAuthentication(utilisateur, motDePasse).build();
    }
}
