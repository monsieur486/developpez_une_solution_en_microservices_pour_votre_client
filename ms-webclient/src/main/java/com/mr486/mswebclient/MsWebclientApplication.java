package com.mr486.mswebclient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Point d'entrée de l'interface web des praticiens.
 *
 * <p>Indépendante des microservices back, elle ne leur parle qu'à travers la gateway.</p>
 *
 * <p><b>Exemple :</b> {@code ./mvnw -pl ms-webclient spring-boot:run} sert l'interface sur le port 8080.</p>
 */
@SpringBootApplication
public class MsWebclientApplication {
    /**
     * Démarre l'application Spring Boot.
     *
     * <p><b>Exemple :</b> {@code java -jar ms-webclient.jar --server.port=8081}.</p>
     *
     * @param args arguments de la ligne de commande, transmis à Spring
     */
    public static void main(String[] args) {
        SpringApplication.run(MsWebclientApplication.class, args);
    }
}
