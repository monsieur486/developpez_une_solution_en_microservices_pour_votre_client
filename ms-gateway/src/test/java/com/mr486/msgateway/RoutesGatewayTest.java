package com.mr486.msgateway;

import static org.hamcrest.Matchers.containsInAnyOrder;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

// Eureka est coupé : aucun microservice n'est inscrit, comme quand ils sont tous arrêtés.
// Le registre de remplacement (SimpleDiscoveryClient) ne connaît alors aucune instance.
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "eureka.client.enabled=false")
@AutoConfigureWebTestClient
@DisplayName("Routes explicites et documentation des API de la gateway")
class RoutesGatewayTest {

    @Autowired
    private WebTestClient client;

    // Non-régression : avec les routes de découverte, un service arrêté perdait sa route et la
    // gateway répondait 404, que le front affichait comme « Page introuvable » au lieu d'une panne.
    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {"/ms-patients/patients", "/ms-notes/patients/1/notes", "/ms-risque/evaluation/1"})
    @DisplayName("un service sans instance répond 503, et non 404")
    void serviceSansInstance_repond503(String chemin) {
        client.get().uri(chemin).exchange()
                .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
    }

    @Test
    @DisplayName("un chemin qui ne correspond à aucun service répond 404")
    void cheminInconnu_repond404() {
        client.get().uri("/ms-inconnu/quelque-chose").exchange()
                .expectStatus().isNotFound();
    }

    // Le sélecteur de l'interface Swagger doit proposer chaque contrat, lu à travers la route du
    // service : un oubli dans application.yml rendrait une API invisible dans la documentation.
    @Test
    @DisplayName("la configuration Swagger liste les contrats des trois microservices")
    void swaggerConfig_listeLesTroisContrats() {
        client.get().uri("/v3/api-docs/swagger-config").exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.urls[*].url").value(containsInAnyOrder(
                        "/ms-patients/v3/api-docs", "/ms-notes/v3/api-docs", "/ms-risque/v3/api-docs"))
                .jsonPath("$['urls.primaryName']").isEqualTo("ms-patients");
    }

    @Test
    @DisplayName("l'interface Swagger est servie par la gateway, sans identifiants")
    void swaggerUi_estServie() {
        client.get().uri("/swagger-ui/index.html").exchange()
                .expectStatus().isOk();
    }
}
