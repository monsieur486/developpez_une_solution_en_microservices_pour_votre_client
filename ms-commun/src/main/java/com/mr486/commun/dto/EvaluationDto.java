package com.mr486.commun.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Évaluation du risque de diabète d'un patient, telle que {@code ms-risque} la restitue.
 *
 * <p>Partagée entre {@code ms-risque}, qui la produit, et {@code ms-webclient}, qui affiche le niveau
 * sur la fiche du patient. Le JSON donne à cette réponse le même format que les autres API et que
 * leurs erreurs.</p>
 *
 * <p><b>Exemple :</b> {@code GET /evaluation/3} renvoie {@code {"patientId":3,"niveau":"In Danger"}}.</p>
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EvaluationDto {

    /** Identifiant du patient évalué, dans {@code ms-patients}. */
    private Long patientId;

    /** Niveau de risque, avec les libellés du cahier des charges, espaces compris. */
    @Schema(allowableValues = {"None", "Borderline", "In Danger", "Early onset"}, example = "In Danger")
    private String niveau;
}
