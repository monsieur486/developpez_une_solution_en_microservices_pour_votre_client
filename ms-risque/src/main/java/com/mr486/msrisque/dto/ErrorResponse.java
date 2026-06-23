package com.mr486.msrisque.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Réponse d'erreur normalisée renvoyée par l'API.
 *
 * <p><b>Exemple :</b> {"status": 404, "message": "Patient introuvable"}.</p>
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {
    private Integer status;
    private String message;
}
