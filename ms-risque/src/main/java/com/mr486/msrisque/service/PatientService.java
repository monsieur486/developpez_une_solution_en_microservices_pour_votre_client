package com.mr486.msrisque.service;

import com.mr486.msrisque.dto.Patient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Récupère les données d'un patient auprès du microservice ms-patients.
 *
 * <p><b>Exemple :</b> getPatientById(7L) retourne le patient d'identifiant 7.</p>
 */
@Service
public class PatientService {

    private final WebClient patientsWebClient;

    /**
     * Construit le service avec le client WebClient de ms-patients.
     *
     * <p><b>Exemple :</b> new PatientService(patientsWebClient) appellera
     * ms-patients au travers du client configuré.</p>
     *
     * @param patientsWebClient le client WebClient configuré pour ms-patients
     */
    public PatientService(@Qualifier("patientsWebClient") WebClient patientsWebClient) {
        this.patientsWebClient = patientsWebClient;
    }

    /**
     * Récupère un patient par son identifiant.
     *
     * <p><b>Exemple :</b> getPatientById(7L) appelle GET /patients/7 et retourne
     * le patient correspondant ; une erreur distante lève
     * {@link com.mr486.msrisque.exception.RemoteServiceException}.</p>
     *
     * @param id identifiant du patient
     * @return le patient correspondant
     */
    public Patient getPatientById(Long id) {
        return patientsWebClient.get()
                .uri("/patients/{id}", id)
                .retrieve()
                .bodyToMono(Patient.class)
                .block();
    }
}
