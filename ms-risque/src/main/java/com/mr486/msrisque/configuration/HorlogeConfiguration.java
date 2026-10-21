package com.mr486.msrisque.configuration;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Fournit l'horloge utilisée pour calculer l'âge des patients.
 *
 * <p>L'injecter plutôt qu'appeler {@code LocalDate.now()} permet aux tests de figer la date avec
 * {@code Clock.fixed}.</p>
 *
 * <p><b>Exemple :</b> en production, l'âge est calculé à la date du jour du serveur.</p>
 */
@Configuration
public class HorlogeConfiguration {
    /**
     * Horloge système, dans le fuseau du serveur.
     *
     * <p><b>Exemple :</b> {@code LocalDate.now(horloge)} donne la date du jour.</p>
     *
     * @return l'horloge système
     */
    @Bean
    public Clock horloge() {
        return Clock.systemDefaultZone();
    }
}
