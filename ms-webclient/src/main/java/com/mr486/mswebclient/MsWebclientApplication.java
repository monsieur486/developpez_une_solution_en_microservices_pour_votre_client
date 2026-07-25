package com.mr486.mswebclient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Point d'entrée du client web : interface Thymeleaf consommant les
 * microservices au travers de la passerelle.
 *
 * <p><b>Exemple :</b> lancer la méthode main démarre le client web sur le port
 * configuré ; /app/patients affiche la liste des patients.</p>
 */
@SpringBootApplication
public class MsWebclientApplication {

    /**
     * Démarre l'application Spring Boot.
     *
     * <p><b>Exemple :</b> main(new String[0]) lance le client web sur le port
     * configuré.</p>
     *
     * @param args les arguments de la ligne de commande
     */
    public static void main(String[] args) {
        SpringApplication.run(MsWebclientApplication.class, args);
    }

}
