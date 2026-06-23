package com.mr486.msrisque.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Fournit l'horloge applicative, injectable pour rendre les calculs de date
 * testables.
 *
 * <p><b>Exemple :</b> en test, remplacer ce bean par un Clock.fixed(...) rend le
 * calcul de l'âge déterministe.</p>
 */
@Configuration
public class ClockConfiguration {

    /**
     * Horloge système par défaut.
     *
     * <p><b>Exemple :</b> clock() retourne Clock.systemDefaultZone().</p>
     *
     * @return l'horloge système
     */
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
