package com.mr486.mspatients.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Réponse d'erreur normalisée renvoyée par l'API.
 *
 * <p><b>Exemple :</b> {"status": 404, "message": "aucun patient trouvé avec l'id: 7"}.</p>
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {
    private Integer status;
    private String message;
}
