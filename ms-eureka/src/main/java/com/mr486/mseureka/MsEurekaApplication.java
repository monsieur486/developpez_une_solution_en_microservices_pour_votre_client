package com.mr486.mseureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Point d'entrée du serveur d'annuaire Eureka : les microservices s'y
 * enregistrent et s'y découvrent mutuellement.
 *
 * <p><b>Exemple :</b> lancer la méthode main démarre l'annuaire ; le tableau de
 * bord Eureka est alors accessible sur le port configuré.</p>
 */
@SpringBootApplication
@EnableEurekaServer
public class MsEurekaApplication {

    /**
     * Démarre l'application Spring Boot.
     *
     * <p><b>Exemple :</b> main(new String[0]) lance l'annuaire Eureka sur le
     * port configuré.</p>
     *
     * @param args les arguments de la ligne de commande
     */
    public static void main(String[] args) {
        SpringApplication.run(MsEurekaApplication.class, args);
    }

}
