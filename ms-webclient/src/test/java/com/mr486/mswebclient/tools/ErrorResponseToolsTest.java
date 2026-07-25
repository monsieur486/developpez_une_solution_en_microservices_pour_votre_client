package com.mr486.mswebclient.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mr486.mswebclient.dto.ErrorMessage;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorResponseToolsTest {

    private final ErrorResponseTools tools = new ErrorResponseTools(new ObjectMapper());

    @Test
    void getErrorMessage_decodeLeCorpsJsonEmbarque() {
        String message = "404 Not Found : {\"status\":404,\"message\":\"Patient introuvable\"}";

        ErrorMessage erreur = tools.getErrorMessage(message, "ms-patients");

        assertThat(erreur.getStatus()).isEqualTo(404);
        assertThat(erreur.getMessage()).isEqualTo("Patient introuvable");
    }

    @Test
    void getErrorMessage_replieSur503QuandLeMessageEstIllisible() {
        ErrorMessage erreur = tools.getErrorMessage("pas de json ici", "ms-patients");

        assertThat(erreur.getStatus()).isEqualTo(503);
        assertThat(erreur.getMessage()).isEqualTo("ms-patients ne répond pas.");
    }

    @Test
    void getErrorMessage_replieSur503QuandLeMessageEstNull() {
        ErrorMessage erreur = tools.getErrorMessage(null, "ms-notes");

        assertThat(erreur.getStatus()).isEqualTo(503);
        assertThat(erreur.getMessage()).isEqualTo("ms-notes ne répond pas.");
    }
}
