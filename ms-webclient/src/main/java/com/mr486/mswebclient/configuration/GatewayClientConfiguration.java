package com.mr486.mswebclient.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration du client REST vers la passerelle : authentification Basic
 * sortante et mapper JSON partagé.
 *
 * <p><b>Exemple :</b> chaque appel du RestTemplate part avec un en-tête
 * "Authorization: Basic …" construit à partir des identifiants applicatifs.</p>
 */
@Configuration
public class GatewayClientConfiguration {

    /**
     * Client REST authentifié utilisé pour appeler la passerelle.
     *
     * <p><b>Exemple :</b> restTemplate("app_user", "app_password") ajoute
     * l'authentification Basic à chaque requête sortante.</p>
     *
     * @param username identifiant applicatif transmis à la passerelle
     * @param password mot de passe applicatif transmis à la passerelle
     * @return le client REST authentifié
     */
    @Bean
    RestTemplate restTemplate(@Value("${app.auth.username}") String username,
                              @Value("${app.auth.password}") String password) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add(new BasicAuthenticationInterceptor(username, password));
        return restTemplate;
    }

    /**
     * Mapper JSON partagé de l'application.
     *
     * <p><b>Exemple :</b> objectMapper() sérialise les objets en JSON indenté,
     * plus lisible dans les journaux et les pages d'erreur.</p>
     *
     * @return le mapper Jackson configuré
     */
    @Bean
    ObjectMapper objectMapper() {
        return new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    }
}
