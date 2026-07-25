package com.mr486.msrisque.exception;

import com.mr486.msrisque.dto.ErrorResponse;
import lombok.Getter;

/**
 * Exception levée lorsqu'un microservice distant retourne une erreur.
 *
 * <p><b>Exemple :</b> un appel WebClient recevant une réponse 404 déclenche une
 * RemoteServiceException portant le statut 404 et le corps d'erreur décodé.</p>
 */
@Getter
public class RemoteServiceException extends RuntimeException {

    private final ErrorResponse error;
    private final int httpStatus;

    /**
     * Construit l'exception à partir du message, du statut et du corps d'erreur.
     *
     * <p><b>Exemple :</b> new RemoteServiceException("Patient introuvable", 404,
     * error) porte le statut 404.</p>
     *
     * @param message    le message d'erreur
     * @param httpStatus le code HTTP retourné par le service distant
     * @param error      le corps d'erreur décodé
     */
    public RemoteServiceException(String message, int httpStatus, ErrorResponse error) {
        super(message);
        this.httpStatus = httpStatus;
        this.error = error;
    }
}
