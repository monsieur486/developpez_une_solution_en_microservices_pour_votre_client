package com.mr486.msrisque.client;

import com.mr486.commun.dto.PatientDto;
import com.mr486.msrisque.configuration.FeignSecurityConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Client Feign de l'API des patients, résolu par son nom dans Eureka.
 *
 * <p>L'appel est direct, de microservice à microservice, sans repasser par la gateway ; il
 * s'authentifie en HTTP Basic grâce à {@link FeignSecurityConfiguration}.</p>
 *
 * <p><b>Exemple :</b> {@code findById(TEST_IN_DANGER)} appelle {@code GET /patients/3} sur une
 * instance de {@code ms-patients}.</p>
 */
@FeignClient(name = "ms-patients", configuration = FeignSecurityConfiguration.class)
public interface PatientClient {
    /**
     * Lit la fiche d'un patient.
     *
     * <p><b>Exemple :</b> un identifiant inconnu fait répondre 404, levé en
     * {@code FeignException.NotFound}.</p>
     *
     * @param id identifiant du patient
     * @return la fiche du patient
     */
    @GetMapping("/patients/{id}")
    PatientDto findById(@PathVariable("id") Long id);
}
