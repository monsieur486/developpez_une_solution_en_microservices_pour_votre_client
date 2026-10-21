package com.mr486.msnotes;

import com.mr486.commun.configuration.SecuriteApiConfiguration;
import com.mr486.commun.exception.GlobalExceptionHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * Point d'entrée du microservice des notes des praticiens.
 *
 * <p>Active explicitement la sécurité et la gestion d'erreurs partagées de {@code ms-commun},
 * qui ne sont pas dans le paquetage balayé par Spring.</p>
 *
 * <p><b>Exemple :</b> {@code ./mvnw -pl ms-notes spring-boot:run} démarre l'API sur le port 9200.</p>
 */
@SpringBootApplication
@Import({
        SecuriteApiConfiguration.class,
        GlobalExceptionHandler.class})
public class MsNotesApplication {
    /**
     * Démarre l'application Spring Boot.
     *
     * <p><b>Exemple :</b> {@code java -jar ms-notes.jar --server.port=9201}.</p>
     *
     * @param args arguments de la ligne de commande, transmis à Spring
     */
    public static void main(String[] args) {
        SpringApplication.run(MsNotesApplication.class, args);
    }
}
