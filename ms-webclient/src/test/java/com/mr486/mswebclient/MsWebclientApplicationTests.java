package com.mr486.mswebclient;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@DisplayName("Démarrage de l'interface web")
class MsWebclientApplicationTests {
    @Test
    @DisplayName("le contexte se charge avec la configuration de sécurité")
    void contextLoads() {
    }
}
