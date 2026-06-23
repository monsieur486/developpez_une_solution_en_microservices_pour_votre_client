package com.mr486.mspatients.configuration;

import com.mr486.mspatients.dto.ErrorResponse;
import com.mr486.mspatients.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleValidation_retourne400EtAgregeLesMessages() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(
                new FieldError("patientForm", "firstName", "le prénom ne peut pas être vide"),
                new FieldError("patientForm", "gender", "le genre ne peut pas être vide")
        ));

        ResponseEntity<ErrorResponse> reponse = handler.handleValidationExceptions(ex);

        assertThat(reponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(reponse.getBody()).isNotNull();
        assertThat(reponse.getBody().getMessage())
                .isEqualTo("le prénom ne peut pas être vide, le genre ne peut pas être vide");
    }

    @Test
    void handleResourceNotFound_retourne404() {
        ResponseEntity<ErrorResponse> reponse =
                handler.handleResourceNotFound(new NotFoundException("introuvable"));

        assertThat(reponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(reponse.getBody()).isNotNull();
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
