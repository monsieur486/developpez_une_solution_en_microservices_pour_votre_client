package com.mr486.commun.dto;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Note d'un patient telle que l'API la restitue.
 *
 * <p>Partagée entre {@code ms-notes}, qui la produit, et {@code ms-webclient}, qui l'affiche dans
 * l'historique : le document MongoDB ne quitte jamais le microservice des notes.</p>
 *
 * <p><b>Exemple :</b> {@code GET /patients/3/notes} renvoie
 * {@code [{"content":"Le patient déclare qu'il fume depuis peu","createdDate":"2026-10-20T16:13:39Z"}]}.</p>
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NoteDto {

    /** Texte de la note, tel que le praticien l'a saisi. */
    private String content;

    /** Instant d'enregistrement de la note, attribué par {@code ms-notes}. */
    private Instant createdDate;
}
