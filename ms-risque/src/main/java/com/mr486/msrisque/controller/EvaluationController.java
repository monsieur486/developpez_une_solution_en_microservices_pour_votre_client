package com.mr486.msrisque.controller;

import com.mr486.msrisque.dto.Risque;
import com.mr486.msrisque.service.EvaluationService;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * Expose l'API REST d'évaluation du risque de diabète d'un patient.
 *
 * <p><b>Exemple :</b> GET /patients/3/evaluation retourne
 * {@code {"level": "In Danger"}} pour le patient TestInDanger du jeu de
 * démonstration (décrit dans le README racine).</p>
 */
@RestController
@RequiredArgsConstructor
@OpenAPIDefinition(info = @Info(title = "Gestion du risque de diabète d'un patient API", version = "v1"))
@SecurityRequirement(name = "basicAuth")
public class EvaluationController {

    private final EvaluationService evaluationService;

    /**
     * Évalue le risque de diabète d'un patient à partir de son identifiant,
     * sans bloquer.
     *
     * <p><b>Exemple :</b> evaluate(3L) émet une réponse 200 de niveau
     * {@code "In Danger"} (patient TestInDanger du jeu de démonstration).</p>
     *
     * @param patientId identifiant du patient à évaluer
     * @return un Mono émettant la réponse HTTP contenant le risque calculé
     */
    @Tag(name = "Évalue le risque de diabète d'un patient par son ID")
    @GetMapping(value = "/patients/{patientId}/evaluation")
    public Mono<ResponseEntity<Risque>> evaluate(@PathVariable Long patientId) {
        return evaluationService.evalueRisque(patientId)
                .map(ResponseEntity::ok);
    }
}
