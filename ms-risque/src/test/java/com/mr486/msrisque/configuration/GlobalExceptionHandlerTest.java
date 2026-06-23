package com.mr486.msrisque.configuration;

import com.mr486.msrisque.dto.ErrorResponse;
import com.mr486.msrisque.exception.RemoteServiceException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.NoHandlerFoundException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    // Sous-classe concrète pour instancier l'exception distante abstraite.
    private static final class ExceptionDistanteDeTest extends RemoteServiceException {
        private ExceptionDistanteDeTest(String message, int httpStatus, ErrorResponse error) {
            super(message, httpStatus, error);
        }
    }

    @Test
    void handleRemote_utiliseLeStatutDistant() {
        ErrorResponse corps = new ErrorResponse(404, "introuvable");
        RemoteServiceException ex = new ExceptionDistanteDeTest("introuvable", 404, corps);

        ResponseEntity<ErrorResponse> reponse = handler.handleRemote(ex);

        assertThat(reponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(reponse.getBody()).isNotNull();
        assertThat(reponse.getBody().getStatus()).isEqualTo(404);
        assertThat(reponse.getBody().getMessage()).isEqualTo("introuvable");
    }

    @Test
    void handleNoHandlerFound_retourne404() {
        NoHandlerFoundException ex = new NoHandlerFoundException("GET", "/inconnu", new HttpHeaders());

        ResponseEntity<ErrorResponse> reponse = handler.handleNoHandlerFoundException(ex);

        assertThat(reponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void handleException_retourne500() {
        ResponseEntity<ErrorResponse> reponse = handler.handleException(new RuntimeException("boom"));

        assertThat(reponse.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(reponse.getBody()).isNotNull();
        assertThat(reponse.getBody().getMessage()).isEqualTo("boom");
    }
}
