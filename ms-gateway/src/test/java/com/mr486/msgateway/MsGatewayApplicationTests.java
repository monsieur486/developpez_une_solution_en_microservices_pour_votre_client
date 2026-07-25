package com.mr486.msgateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Vérifie que le contexte Spring de la passerelle démarre correctement.
 *
 * <p><b>Exemple :</b> contextLoads() échoue si un bean de la passerelle ne peut
 * pas être construit.</p>
 */
@SpringBootTest
class MsGatewayApplicationTests {

    @Test
    void contextLoads() {
        // le chargement du contexte suffit : un échec de configuration fait échouer le test
    }

}
