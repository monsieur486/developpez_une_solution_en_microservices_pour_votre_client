package com.mr486.msrisque.controller;

import com.mr486.commun.dto.ErrorResponse;
import com.mr486.commun.dto.EvaluationDto;
import com.mr486.msrisque.service.EvaluationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
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
 * <p>Le résultat est un {@link EvaluationDto} en JSON, comme les erreurs, que le gestionnaire
 * d'erreurs commun met en forme (patient inconnu, service injoignable ou refusant l'appel). Un seul
 * format permet à Spring de toujours répondre au client qui demande du JSON.</p>
 *
 * <p><b>Exemple :</b> {@code GET /evaluation/2} répond {@code 200} avec
 * {@code {"patientId":2,"niveau":"Borderline"}}.</p>
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
     * <p><b>Exemple :</b> {@code GET /evaluation/3} répond {@code {"patientId":3,"niveau":"In Danger"}} ;
     * un identifiant inconnu répond {@code 404}, un appel sans identifiants {@code 401}.</p>
     *
     * @param patientId identifiant du patient dans {@code ms-patients}
     * @return {@code 200} avec le patient et son niveau de risque
     */
    @Operation(summary = "Évalue le risque de diabète d'un patient",
            description = "Croise l'âge et le genre du patient avec les termes déclencheurs de ses notes.")
    @ApiResponse(responseCode = "200", description = "Patient évalué et son niveau de risque")
    @ApiResponse(responseCode = "401", description = "Identifiants absents ou invalides", content = @Content)
    @ApiResponse(responseCode = "404", description = "Aucun patient avec cet identifiant",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "502", description = "ms-patients ou ms-notes a refusé l'appel",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "503", description = "ms-patients ou ms-notes est injoignable",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping(value = "/evaluation/{patientId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EvaluationDto> evaluate(@PathVariable Long patientId) {
        return ResponseEntity.ok(new EvaluationDto(patientId, evaluationService.evaluationDuRisque(patientId)));
    }
}
