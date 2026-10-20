package com.mr486.msnotes.configuration;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Fournit l'horloge qui date les notes.
 *
 * <p>Injecter un {@link Clock} plutôt qu'appeler {@code Instant.now()} rend la date maîtrisable :
 * un test passe {@code Clock.fixed(...)} et connaît d'avance la date attendue.</p>
 *
 * <p><b>Exemple :</b> une note enregistrée le 20 octobre 2026 à 18 h 13 (heure de Paris) porte
 * {@code createdDate = 2026-10-20T16:13:00Z}.</p>
 */
@Configuration
public class HorlogeConfiguration {

    /**
     * Horloge système, utilisée en fonctionnement normal.
     *
     * <p><b>Exemple :</b> {@code Instant.now(horloge())} donne l'instant présent.</p>
     *
     * @return l'horloge du système, dans le fuseau par défaut
     */
    @Bean
    public Clock horloge() {
        return Clock.systemDefaultZone();
    }
}
