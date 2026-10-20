package com.mr486.msrisque;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@DisplayName("Démarrage du microservice d'évaluation du risque")
class MsRisqueApplicationTests {
    @Test
    @DisplayName("le contexte se charge avec les clients Feign déclarés")
    void contextLoads() {
    }
}
