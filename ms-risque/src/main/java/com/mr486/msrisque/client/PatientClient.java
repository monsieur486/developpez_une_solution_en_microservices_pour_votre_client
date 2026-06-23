package com.mr486.msrisque.client;

import com.mr486.msrisque.configuration.FeignConfiguration;
import com.mr486.msrisque.dto.Patient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Client Feign vers le microservice ms-patients.
 *
 * <p><b>Exemple :</b> findById(7L) appelle GET /patients/7 et retourne le
 * patient 7.</p>
 */
@FeignClient(name = "ms-patients", configuration = FeignConfiguration.class)
public interface PatientClient {

    /**
     * Récupère un patient par son identifiant.
     *
     * <p><b>Exemple :</b> findById(7L) retourne le patient d'identifiant 7.</p>
     *
     * @param id identifiant du patient
     * @return le patient correspondant
     */
    @GetMapping("/patients/{id}")
    Patient findById(@PathVariable("id") Long id);
}
