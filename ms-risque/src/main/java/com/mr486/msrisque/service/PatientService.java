package com.mr486.msrisque.service;

import com.mr486.msrisque.client.PatientClient;
import com.mr486.msrisque.dto.Patient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Récupère les données d'un patient auprès du microservice ms-patients.
 *
 * <p><b>Exemple :</b> getPatientById(7L) retourne le patient d'identifiant 7.</p>
 */
@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientClient patientClient;

    /**
     * Récupère un patient par son identifiant.
     *
     * <p><b>Exemple :</b> getPatientById(7L) retourne le patient d'identifiant 7.</p>
     *
     * @param id identifiant du patient
     * @return le patient correspondant
     */
    public Patient getPatientById(Long id) {
        return patientClient.findById(id);
    }
}
