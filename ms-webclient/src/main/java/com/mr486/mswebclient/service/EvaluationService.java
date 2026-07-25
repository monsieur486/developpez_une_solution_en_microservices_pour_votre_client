package com.mr486.mswebclient.service;

import com.mr486.mswebclient.dto.Risque;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Accède à l'évaluation du risque calculée par ms-risque au travers de la
 * passerelle.
 *
 * <p><b>Exemple :</b> getEvaluationByPatientId(7L) retourne le risque calculé
 * pour le patient 7.</p>
 */
@Service
public class EvaluationService {

    private final RestTemplate restTemplate;
    private final String gatewayBase;

    /**
     * Construit le service avec le client REST et l'URL de la passerelle.
     *
     * <p><b>Exemple :</b> new EvaluationService(restTemplate, "http://localhost:9000")
     * appellera http://localhost:9000/ms-risque/patients/7/evaluation.</p>
     *
     * @param restTemplate le client REST authentifié vers la passerelle
     * @param gatewayBase  l'URL de base de la passerelle
     */
    public EvaluationService(RestTemplate restTemplate,
                             @Value("${app.gateway.base-url}") String gatewayBase) {
        this.restTemplate = restTemplate;
        this.gatewayBase = gatewayBase;
    }

    /**
     * Retourne l'évaluation du risque de diabète d'un patient.
     *
     * <p><b>Exemple :</b> getEvaluationByPatientId(7L) retourne un Risque dont le
     * niveau vaut par exemple "Borderline".</p>
     *
     * @param patientId identifiant du patient à évaluer
     * @return le risque calculé pour ce patient
     */
    public Risque getEvaluationByPatientId(Long patientId) {
        ResponseEntity<Risque> response = restTemplate.exchange(
                gatewayBase + "/ms-risque/patients/" + patientId + "/evaluation",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );
        return response.getBody();
    }
}
