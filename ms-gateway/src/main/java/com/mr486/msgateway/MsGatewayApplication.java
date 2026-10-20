package com.mr486.msgateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Point d'entrée unique des API : la gateway route chaque préfixe vers son microservice.
 *
 * <p>Les routes, déclarées dans {@code application.yml}, visent une adresse {@code lb://} résolue
 * dans Eureka et répartie entre les instances ; sans instance, elles répondent 503. La gateway
 * sert aussi l'interface Swagger qui réunit les contrats des trois microservices.</p>
 *
 * <p><b>Exemple :</b> {@code GET /ms-patients/patients/1} est transmis à une instance de
 * {@code ms-patients} sous la forme {@code GET /patients/1}.</p>
 */
@SpringBootApplication
public class MsGatewayApplication {
  /**
   * Démarre la gateway.
   *
   * <p><b>Exemple :</b> {@code ./mvnw -pl ms-gateway spring-boot:run}, depuis la racine du dépôt.</p>
   *
   * @param args arguments de la ligne de commande, transmis à Spring
   */
  public static void main(String[] args) {
    SpringApplication.run(MsGatewayApplication.class, args);
  }

}
