package com.mr486.msgateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Point d'entrée de la passerelle (Spring Cloud Gateway) : route les requêtes
 * vers les microservices enregistrés auprès d'Eureka.
 *
 * <p><b>Exemple :</b> lancer la méthode main démarre la passerelle sur le port
 * configuré ; GET /ms-patients/patients est routé vers ms-patients.</p>
 */
@SpringBootApplication
public class MsGatewayApplication {

    /**
     * Démarre l'application Spring Boot.
     *
     * <p><b>Exemple :</b> main(new String[0]) lance la passerelle sur le port
     * configuré.</p>
     *
     * @param args les arguments de la ligne de commande
     */
    public static void main(String[] args) {
        SpringApplication.run(MsGatewayApplication.class, args);
    }

}
