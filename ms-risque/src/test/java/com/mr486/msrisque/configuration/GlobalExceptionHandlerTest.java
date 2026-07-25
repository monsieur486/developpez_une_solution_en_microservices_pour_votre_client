package com.mr486.msrisque.configuration;

import com.mr486.msrisque.dto.ErrorResponse;
import com.mr486.msrisque.exception.RemoteServiceException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleRemote_utiliseLeStatutDistant() {
        ErrorResponse corps = new ErrorResponse(404, "introuvable");
        RemoteServiceException ex = new RemoteServiceException("introuvable", 404, corps);

        ResponseEntity<ErrorResponse> reponse = handler.handleRemote(ex);

        assertThat(reponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(reponse.getBody()).isNotNull();
        assertThat(reponse.getBody().getStatus()).isEqualTo(404);
        assertThat(reponse.getBody().getMessage()).isEqualTo("introuvable");
    }

    @Test
    void handleResponseStatus_respecteLeStatutPorte() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.NOT_FOUND, "introuvable");

        ResponseEntity<ErrorResponse> reponse = handler.handleResponseStatus(ex);

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
