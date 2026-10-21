package com.mr486.mseureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Registre des services : chaque microservice s'y inscrit sous son nom au démarrage.
 *
 * <p>La gateway et {@code ms-risque} y résolvent les adresses des instances, ce qui permet de
 * lancer plusieurs réplicas d'un même service sans configurer leurs adresses.</p>
 *
 * <p><b>Exemple :</b> une fois lancé, http://localhost:8761 liste les instances inscrites.</p>
 */
@SpringBootApplication
@EnableEurekaServer
public class MsEurekaApplication {
  /**
   * Démarre le registre Eureka.
   *
   * <p><b>Exemple :</b> {@code ./mvnw -pl ms-eureka spring-boot:run}, depuis la racine du dépôt.</p>
   *
   * @param args arguments de la ligne de commande, transmis à Spring
   */
  public static void main(String[] args) {
    SpringApplication.run(MsEurekaApplication.class, args);
  }

}
