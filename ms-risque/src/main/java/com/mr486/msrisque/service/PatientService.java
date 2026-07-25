package com.mr486.msrisque.service;

import com.mr486.msrisque.dto.Patient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Récupère les données d'un patient auprès du microservice ms-patients.
 *
 * <p><b>Exemple :</b> getPatientById(7L) retourne un Mono émettant le patient
 * d'identifiant 7.</p>
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
     * Récupère un patient par son identifiant, sans bloquer.
     *
     * <p><b>Exemple :</b> getPatientById(7L) appelle GET /patients/7 et émet le
     * patient correspondant ; une erreur distante propage
     * {@link com.mr486.msrisque.exception.RemoteServiceException}.</p>
     *
     * @param id identifiant du patient
     * @return un Mono émettant le patient correspondant
     */
    public Mono<Patient> getPatientById(Long id) {
        return patientsWebClient.get()
                .uri("/patients/{id}", id)
                .retrieve()
                .bodyToMono(Patient.class);
    }
}
