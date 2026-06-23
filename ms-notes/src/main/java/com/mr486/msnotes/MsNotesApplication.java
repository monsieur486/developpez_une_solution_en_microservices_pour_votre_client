package com.mr486.msnotes;

import io.mongock.runner.springboot.EnableMongock;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Point d'entrée du microservice de gestion des notes patient.
 *
 * <p><b>Exemple :</b> exécuter {@code java -jar ms-notes.jar} démarre l'application
 * Spring Boot, déclenche les migrations Mongock et expose l'API REST des notes.</p>
 */
@SpringBootApplication
@EnableMongock
public class MsNotesApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsNotesApplication.class, args);
    }

}
