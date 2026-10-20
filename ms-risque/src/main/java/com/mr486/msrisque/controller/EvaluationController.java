package com.mr486.msrisque.controller;

import com.mr486.commun.dto.ErrorResponse;
import com.mr486.msrisque.service.EvaluationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Évaluation", description = "Risque de diabète de type 2 d'un patient")
@SecurityRequirement(name = "basicAuth")
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
    @Operation(summary = "Évalue le risque de diabète d'un patient",
            description = "Croise l'âge et le genre du patient avec les termes déclencheurs de ses notes.")
    @ApiResponse(responseCode = "200", description = "Niveau de risque, en texte brut",
            content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE,
                    schema = @Schema(type = "string",
                            allowableValues = {"None", "Borderline", "In Danger", "Early onset"}),
                    examples = @ExampleObject(value = "In Danger")))
    @ApiResponse(responseCode = "401", description = "Identifiants absents ou invalides", content = @Content)
    @ApiResponse(responseCode = "404", description = "Aucun patient avec cet identifiant",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "502", description = "ms-patients ou ms-notes a refusé l'appel",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "503", description = "ms-patients ou ms-notes est injoignable",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping(value = "/evaluation/{patientId}", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> evaluate(@PathVariable Long patientId) {
        return ResponseEntity.ok(evaluationService.evaluationDuRisque(patientId));
    }
}
