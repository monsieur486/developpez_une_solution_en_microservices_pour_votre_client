package com.mr486.msnotes.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Réponse d'erreur normalisée renvoyée par l'API.
 *
 * <p><b>Exemple :</b> une validation échouée produit {@code {"status": 400,
 * "message": "la note ne doit pas être vide"}}.</p>
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {

    private Integer status;
    private String message;
}
