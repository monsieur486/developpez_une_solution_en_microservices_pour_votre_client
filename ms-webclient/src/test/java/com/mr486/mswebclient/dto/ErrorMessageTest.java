package com.mr486.mswebclient.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorMessageTest {

    @Test
    void critical_retourneTruePourUneErreurServeur() {
        assertThat(new ErrorMessage(503, "indisponible").critical()).isTrue();
    }

    @Test
    void critical_retourneFalsePourUneErreurClient() {
        assertThat(new ErrorMessage(404, "introuvable").critical()).isFalse();
    }

    @Test
    void critical_retourneFalseSansStatut() {
        assertThat(new ErrorMessage(null, "inconnu").critical()).isFalse();
    }
}
