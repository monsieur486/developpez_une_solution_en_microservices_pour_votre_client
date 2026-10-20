package com.mr486.msrisque.controller;

import com.mr486.msrisque.service.EvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * API REST de l'évaluation du risque de diabète.
 *
 * <p>La réponse est le libellé du niveau en texte brut, sans enveloppe JSON. Les erreurs (patient
 * inconnu, service injoignable) sont traduites en JSON par le gestionnaire d'erreurs commun.</p>
 *
 * <p><b>Exemple :</b> {@code GET /evaluation/2} répond {@code 200} avec le corps {@code Borderline}.</p>
 */
@RestController
@RequiredArgsConstructor
public class EvaluationController {
    private final EvaluationService evaluationService;

    /**
     * Évalue le risque de diabète d'un patient.
     *
     * <p><b>Exemple :</b> {@code GET /evaluation/3} répond {@code In Danger} ; un identifiant inconnu
     * répond {@code 404}, un appel sans identifiants {@code 401}.</p>
     *
     * @param patientId identifiant du patient dans {@code ms-patients}
     * @return {@code 200} avec le libellé du niveau en {@code text/plain}
     */
    @GetMapping(value = "/evaluation/{patientId}", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> evaluate(@PathVariable Long patientId) {
        return ResponseEntity.ok(evaluationService.evaluationDuRisque(patientId));
    }
}
