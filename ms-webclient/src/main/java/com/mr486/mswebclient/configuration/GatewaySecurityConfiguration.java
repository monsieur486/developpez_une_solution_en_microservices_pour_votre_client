package com.mr486.mswebclient.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configure le client HTTP qui appelle les microservices à travers la gateway.
 *
 * <p>Les API back exigent une authentification HTTP Basic : chaque requête sortante porte les
 * identifiants techniques lus en configuration.</p>
 *
 * <p><b>Exemple :</b> un appel à {@code /ms-patients/patients} part avec l'en-tête
 * {@code Authorization: Basic …}.</p>
 */
@Configuration
public class GatewaySecurityConfiguration {
    /**
     * Construit le client HTTP authentifié vers la gateway.
     *
     * <p>Passer par le {@link RestTemplateBuilder} de Spring Boot garantit la même configuration
     * Jackson que le reste de l'application : les dates partent au format ISO
     * ({@code "1966-12-31"}) et non en tableau ({@code [1966,12,31]}).</p>
     *
     * <p><b>Exemple :</b> avec {@code APP_USERNAME=app_user}, chaque requête porte
     * {@code Authorization: Basic YXBwX3VzZXI6…}.</p>
     *
     * @param constructeur constructeur de client fourni et configuré par Spring Boot
     * @param utilisateur  identifiant technique attendu par les API back
     * @param motDePasse   mot de passe technique attendu par les API back
     * @return le client HTTP utilisé par {@code ClientPasserelle}
     */
    @Bean
    public RestTemplate restTemplate(
            RestTemplateBuilder constructeur,
            @Value("${app.auth.username}") String utilisateur,
            @Value("${app.auth.password}") String motDePasse) {
        return constructeur.basicAuthentication(utilisateur, motDePasse).build();
    }
}
