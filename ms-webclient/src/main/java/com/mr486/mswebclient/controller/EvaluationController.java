package com.mr486.mswebclient.controller;

import com.mr486.mswebclient.exception.GatewayException;
import com.mr486.mswebclient.service.EvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Mono;

/**
 * Page de l'évaluation du risque de diabète d'un patient.
 *
 * <p><b>Exemple :</b> GET /app/patients/3/evaluation affiche {@code "In Danger"}
 * pour le patient TestInDanger du jeu de démonstration (décrit dans le README
 * racine).</p>
 */
@Controller
@RequestMapping("/app")
@RequiredArgsConstructor
@Slf4j
public class EvaluationController {

    private final EvaluationService evaluationService;

    /**
     * Affiche l'évaluation du risque de diabète d'un patient, sans bloquer.
     *
     * <p><b>Exemple :</b> GET /app/patients/3/evaluation retourne la vue
     * "evaluation/evaluation" avec le niveau de risque dans le modèle.</p>
     *
     * @param model     le modèle de la vue
     * @param patientId identifiant du patient à évaluer
     * @return un Mono émettant le nom de la vue de l'évaluation
     */
    @GetMapping("/patients/{patientId}/evaluation")
    public Mono<String> getEvaluation(Model model, @PathVariable Long patientId) {
        model.addAttribute("patientId", patientId);
        return evaluationService.getEvaluationByPatientId(patientId)
                .map(evaluation -> {
                    model.addAttribute("evaluation", evaluation.getLevel());
                    return "evaluation/evaluation";
                })
                .onErrorResume(GatewayException.class, ex -> {
                    log.warn("échec de l'évaluation du patient {} : {}", patientId, ex.getMessage());
                    model.addAttribute("errorMessage", ex.getErrorMessage());
                    return Mono.just("evaluation/evaluation");
                });
    }
}
