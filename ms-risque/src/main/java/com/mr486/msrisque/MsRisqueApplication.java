package com.mr486.msrisque;

import com.mr486.commun.configuration.SecuriteApiConfiguration;
import com.mr486.commun.exception.GlobalExceptionHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;

/**
 * Point d'entrée du microservice d'évaluation du risque de diabète.
 *
 * <p>Le service n'a pas de base de données : il interroge {@code ms-patients} et {@code ms-notes}
 * par des clients Feign résolus dans Eureka. Il réutilise la sécurité HTTP Basic et le gestionnaire
 * d'erreurs de {@code ms-commun}, comme les autres microservices.</p>
 *
 * <p><b>Exemple :</b> une fois lancé, {@code GET /evaluation/4} répond {@code Early onset}.</p>
 */
@SpringBootApplication
@EnableFeignClients
@Import({
        SecuriteApiConfiguration.class,
        GlobalExceptionHandler.class})
public class MsRisqueApplication {
    /**
     * Démarre l'application Spring Boot.
     *
     * <p><b>Exemple :</b> {@code ./mvnw -pl ms-risque spring-boot:run}, depuis la racine du dépôt.</p>
     *
     * @param args arguments de la ligne de commande, transmis à Spring
     */
    public static void main(String[] args) {
        SpringApplication.run(MsRisqueApplication.class, args);
    }
}
