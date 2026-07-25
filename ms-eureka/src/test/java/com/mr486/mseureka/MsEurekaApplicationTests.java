package com.mr486.mseureka;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Vérifie que le contexte Spring du serveur Eureka démarre correctement.
 *
 * <p><b>Exemple :</b> contextLoads() échoue si un bean de l'annuaire ne peut
 * pas être construit.</p>
 */
@SpringBootTest
class MsEurekaApplicationTests {

    @Test
    void contextLoads() {
        // le chargement du contexte suffit : un échec de configuration fait échouer le test
    }

}
