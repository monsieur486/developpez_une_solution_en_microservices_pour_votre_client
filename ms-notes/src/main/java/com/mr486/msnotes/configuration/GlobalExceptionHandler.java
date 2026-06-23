package com.mr486.msnotes.configuration;

import com.mr486.msnotes.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

/**
 * Gestionnaire global des exceptions de l'API : traduit chaque erreur en réponse
 * HTTP normalisée ({@link ErrorResponse}).
 *
 * <p><b>Exemple :</b> une erreur de validation produit une réponse HTTP 400 listant
 * les champs fautifs ; une route inconnue produit une réponse HTTP 404.</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Traite les erreurs de validation des formulaires.
     *
     * <p><b>Exemple :</b> un POST /patients/2/notes sans contenu retourne une
     * réponse 400 dont le message agrège les libellés d'erreur.</p>
     *
     * @param ex l'exception de validation levée
     * @return une réponse HTTP 400 décrivant les champs invalides
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        StringBuilder messages = new StringBuilder();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            messages.append(fe.getDefaultMessage());
            messages.append(", ");
        }
        // Retire la dernière virgule de séparation si au moins un message est présent.
        if (messages.length() > 2) {
            messages.setLength(messages.length() - 2);
        }
        return build(messages.toString(), HttpStatus.BAD_REQUEST);
    }

    /**
     * Traite les routes inconnues (aucun handler trouvé).
     *
     * <p><b>Exemple :</b> un appel sur une URL inexistante retourne une réponse
     * HTTP 404.</p>
     *
     * @param ex l'exception levée
     * @return une réponse HTTP 404 avec le message d'erreur
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandlerFoundException(NoHandlerFoundException ex) {
        return build(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    /**
     * Traite toute exception non gérée par ailleurs.
     *
     * <p><b>Exemple :</b> une erreur inattendue retourne une réponse HTTP 500.</p>
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
