package com.mr486.mspatients;

import com.mr486.commun.configuration.DocumentationApiConfiguration;
import com.mr486.commun.configuration.SecuriteApiConfiguration;
import com.mr486.commun.exception.GlobalExceptionHandler;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * Point d'entrée du microservice de gestion des patients.
 *
 * <p>Active explicitement la sécurité et la gestion d'erreurs partagées de {@code ms-commun},
 * qui ne sont pas dans le paquetage balayé par Spring.</p>
 *
 * <p><b>Exemple :</b> {@code ./mvnw -pl ms-patients spring-boot:run} démarre l'API sur le port 9100.</p>
 */
@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = "Gestion des patients API", version = "v1"))
@Import({
        SecuriteApiConfiguration.class,
        DocumentationApiConfiguration.class,
        GlobalExceptionHandler.class})
public class MsPatientsApplication {
    /**
     * Démarre l'application Spring Boot.
     *
     * <p><b>Exemple :</b> {@code java -jar ms-patients.jar --server.port=9101}.</p>
     *
     * @param args arguments de la ligne de commande, transmis à Spring
     */
    public static void main(String[] args) {
        SpringApplication.run(MsPatientsApplication.class, args);
    }
}
