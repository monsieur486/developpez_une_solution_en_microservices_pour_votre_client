package com.mr486.mswebclient.service;

import com.mr486.mswebclient.configuration.ConstantesWebclient;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

/**
 * Demande à {@code ms-risque}, à travers la gateway, le niveau de risque de diabète d'un patient.
 *
 * <p>Les erreurs sont traduites par {@link ClientPasserelle} : une panne de {@code ms-risque}
 * devient une {@code ServiceIndisponibleException}, que la fiche patient remplace par un
 * avertissement.</p>
 *
 * <p><b>Exemple :</b> {@code evalueLeRisque(TEST_EARLY_ONSET)} retourne {@code "Early onset"}.</p>
 */
@Service
@RequiredArgsConstructor
public class EvaluationApiService {
    private final ClientPasserelle clientPasserelle;

    /**
     * Retourne le niveau de risque d'un patient, tel que {@code ms-risque} l'écrit.
     *
     * <p><b>Exemple :</b> {@code evalueLeRisque(TEST_IN_DANGER)} appelle
     * {@code GET /ms-risque/evaluation/3} et retourne {@code "In Danger"}.</p>
     *
     * @param patientId identifiant du patient
     * @return {@code None}, {@code Borderline}, {@code In Danger} ou {@code Early onset}
     * @throws com.mr486.commun.exception.ServiceIndisponibleException si {@code ms-risque} est
     *     injoignable ou en erreur
     * @throws com.mr486.commun.exception.ResourceNotFoundException si le patient est inconnu
     */
    public String evalueLeRisque(Long patientId) {
        return clientPasserelle.echange(
                HttpMethod.GET,
                ConstantesWebclient.CHEMIN_EVALUATION + patientId,
                null,
                new ParameterizedTypeReference<String>() {},
                ConstantesWebclient.SERVICE_RISQUE);
    }
}
