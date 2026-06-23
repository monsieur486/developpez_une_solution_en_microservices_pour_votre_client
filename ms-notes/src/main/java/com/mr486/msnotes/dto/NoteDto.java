package com.mr486.msnotes.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Données reçues pour créer une note : le contenu textuel saisi.
 *
 * <p><b>Exemple :</b> un POST avec {@code {"content": "RAS"}} crée une note ; un
 * contenu vide déclenche une erreur de validation HTTP 400.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoteDto {

    @NotBlank(message = "la note ne doit pas être vide")
    private String content;
}
