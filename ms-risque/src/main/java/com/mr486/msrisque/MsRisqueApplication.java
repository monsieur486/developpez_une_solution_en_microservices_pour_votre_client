package com.mr486.msrisque;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Point d'entrée du microservice ms-risque.
 *
 * <p><b>Exemple :</b> lancer la méthode main démarre l'application Spring Boot
 * et ses clients WebClient vers ms-patients et ms-notes.</p>
 */
@SpringBootApplication
public class MsRisqueApplication {

    /**
     * Démarre l'application Spring Boot.
     *
     * <p><b>Exemple :</b> main(new String[0]) lance le microservice sur le port
     * configuré.</p>
     *
     * @param args les arguments de la ligne de commande
     */
    public static void main(String[] args) {
        SpringApplication.run(MsRisqueApplication.class, args);
    }
}
