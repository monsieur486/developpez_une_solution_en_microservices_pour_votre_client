package com.mr486.mswebclient.controller;

import com.mr486.mswebclient.dto.ErrorMessage;
import com.mr486.mswebclient.dto.Risque;
import com.mr486.mswebclient.service.EvaluationService;
import com.mr486.mswebclient.tools.ErrorResponseTools;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Page de l'évaluation du risque de diabète d'un patient.
 *
 * <p><b>Exemple :</b> GET /app/patients/7/evaluation affiche le niveau de
 * risque calculé pour le patient 7.</p>
 */
@Controller
@RequestMapping("/app")
@RequiredArgsConstructor
@Slf4j
public class EvaluationController {

    /** Nom du microservice cité dans les messages d'erreur. */
    private static final String MICROSERVICE = "ms-risque";

    private final EvaluationService evaluationService;
    private final ErrorResponseTools errorResponseTools;

    /**
     * Affiche l'évaluation du risque de diabète d'un patient.
     *
     * <p><b>Exemple :</b> GET /app/patients/7/evaluation retourne la vue
     * "evaluation/evaluation" avec le niveau de risque dans le modèle.</p>
     *
     * @param model     le modèle de la vue
     * @param patientId identifiant du patient à évaluer
     * @return le nom de la vue de l'évaluation
     */
    @GetMapping("/patients/{patientId}/evaluation")
    public String getEvaluation(Model model, @PathVariable Long patientId) {
        try {
            Risque evaluation = evaluationService.getEvaluationByPatientId(patientId);
            model.addAttribute("evaluation", evaluation.getLevel());
            model.addAttribute("patientId", patientId);
        } catch (Exception ex) {
            log.warn("échec de l'évaluation du patient {} : {}", patientId, ex.getMessage());
            ErrorMessage errorMessage = errorResponseTools.getErrorMessage(ex.getMessage(), MICROSERVICE);
            model.addAttribute("errorMessage", errorMessage);
            model.addAttribute("patientId", patientId);
        }
        return "evaluation/evaluation";
    }
}
