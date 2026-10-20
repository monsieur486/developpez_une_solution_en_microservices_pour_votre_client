package com.mr486.msgateway;

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
@DisplayName("Routes explicites de la gateway")
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
}
