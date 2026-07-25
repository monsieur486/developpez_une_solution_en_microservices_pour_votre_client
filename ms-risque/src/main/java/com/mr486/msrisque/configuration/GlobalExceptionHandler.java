package com.mr486.msrisque.configuration;

import com.mr486.msrisque.dto.ErrorResponse;
import com.mr486.msrisque.exception.RemoteServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

/**
 * Gestionnaire global des exceptions de l'API : traduit chaque erreur en
 * réponse HTTP normalisée ({@link ErrorResponse}).
 *
 * <p><b>Exemple :</b> une RemoteServiceException portant le statut 404 produit
 * une réponse HTTP 404 contenant le message d'erreur.</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Traite les erreurs remontées par les services distants.
     *
     * <p><b>Exemple :</b> handleRemote(ex) avec ex de statut 404 retourne une
     * réponse HTTP 404 et le message de l'exception.</p>
     *
     * @param ex l'exception distante levée
     * @return la réponse HTTP correspondant au statut distant
     */
    @ExceptionHandler(RemoteServiceException.class)
    public ResponseEntity<ErrorResponse> handleRemote(RemoteServiceException ex) {
        return build(ex.getMessage(), HttpStatus.valueOf(ex.getHttpStatus()));
    }

    /**
     * Traite les exceptions à statut porté (route inconnue, requête invalide…).
     *
     * <p><b>Exemple :</b> un appel sur une URL inexistante lève une
     * ResponseStatusException 404 et retourne une réponse HTTP 404.</p>
     *
     * @param ex l'exception levée
     * @return une réponse HTTP au statut porté par l'exception
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatus(ResponseStatusException ex) {
        return build(ex.getReason(), HttpStatus.valueOf(ex.getStatusCode().value()));
    }

    /**
     * Traite toute exception non gérée par ailleurs.
     *
     * <p><b>Exemple :</b> une NullPointerException non interceptée retourne une
     * réponse HTTP 500.</p>
     *
     * @param ex l'exception levée
     * @return une réponse HTTP 500 avec le message d'erreur
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        return build(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Construit la réponse HTTP normalisée à partir d'un message et d'un statut.
    private ResponseEntity<ErrorResponse> build(String message, HttpStatus status) {
        ErrorResponse body = ErrorResponse.builder()
                .status(status.value())
                .message(message)
                .build();
        return ResponseEntity.status(status).body(body);
    }
}
