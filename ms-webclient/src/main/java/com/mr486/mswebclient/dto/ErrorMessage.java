package com.mr486.mswebclient.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * Message d'erreur affiché à l'utilisateur lorsqu'un microservice répond en
 * erreur.
 *
 * <p><b>Exemple :</b> un ErrorMessage de statut 503 est critique ; un 404 ne
 * l'est pas.</p>
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ErrorMessage {

    private Integer status;
    private String message;

    /**
     * Indique si l'erreur est critique (erreur serveur, statut 5xx).
     *
     * <p><b>Exemple :</b> un statut 503 retourne true ; un statut 404 ou null
     * retourne false.</p>
     *
     * @return true si le statut est une erreur serveur
     */
    public Boolean critical() {
        return this.status != null && this.status >= HttpStatus.INTERNAL_SERVER_ERROR.value();
    }
}
