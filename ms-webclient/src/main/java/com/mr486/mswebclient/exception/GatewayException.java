package com.mr486.mswebclient.exception;

import com.mr486.mswebclient.dto.ErrorMessage;
import lombok.Getter;

/**
 * Exception levée lorsqu'un appel à la passerelle échoue : elle porte le
 * message d'erreur prêt à afficher à l'utilisateur.
 *
 * <p><b>Exemple :</b> une réponse 404 de ms-patients lève une GatewayException
 * dont l'ErrorMessage porte le statut 404 et le message distant.</p>
 */
@Getter
public class GatewayException extends RuntimeException {

    private final ErrorMessage errorMessage;

    /**
     * Construit l'exception à partir du message d'erreur à afficher.
     *
     * <p><b>Exemple :</b> new GatewayException(new ErrorMessage(503,
     * "ms-patients ne répond pas.")) porte un message critique.</p>
     *
     * @param errorMessage le message d'erreur destiné à l'utilisateur
     */
    public GatewayException(ErrorMessage errorMessage) {
        super(errorMessage.getMessage());
        this.errorMessage = errorMessage;
    }
}
