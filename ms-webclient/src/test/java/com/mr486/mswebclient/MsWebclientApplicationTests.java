package com.mr486.mswebclient;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Vérifie que le contexte Spring du client web démarre correctement.
 *
 * <p><b>Exemple :</b> contextLoads() échoue si un bean de l'application ne peut
 * pas être construit.</p>
 */
@SpringBootTest
class MsWebclientApplicationTests {

    @Test
    void contextLoads() {
        // le chargement du contexte suffit : un échec de configuration fait échouer le test
    }

}
