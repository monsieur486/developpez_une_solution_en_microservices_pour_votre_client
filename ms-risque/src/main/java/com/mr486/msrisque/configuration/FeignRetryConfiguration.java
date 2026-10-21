package com.mr486.msrisque.configuration;

import feign.Retryer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Fait réessayer les appels Feign qui échouent sur une erreur réseau passagère.
 *
 * <p>Feign ne réessaie que les erreurs « réessayables » (connexion refusée, délai dépassé) : un
 * 404 ou un 401 n'est jamais rejoué. Trois tentatives au plus, avec une attente croissante.</p>
 *
 * <p><b>Exemple :</b> une instance de {@code ms-notes} qui redémarre est réessayée après 100 ms.</p>
 */
@Configuration
public class FeignRetryConfiguration {
    private static final long ATTENTE_INITIALE_MS = 100L;

    private static final long ATTENTE_MAXIMALE_MS = 1000L;

    private static final int NOMBRE_DE_TENTATIVES = 3;

    /**
     * Stratégie de nouvelle tentative partagée par les clients Feign.
     *
     * <p><b>Exemple :</b> attente de 100 ms, puis plus longue, sans dépasser 1 s entre deux essais.</p>
     *
     * @return trois tentatives au plus, de 100 ms à 1 s d'attente
     */
    @Bean
    public Retryer feignRetryer() {
        return new Retryer.Default(ATTENTE_INITIALE_MS, ATTENTE_MAXIMALE_MS, NOMBRE_DE_TENTATIVES);
    }
}
