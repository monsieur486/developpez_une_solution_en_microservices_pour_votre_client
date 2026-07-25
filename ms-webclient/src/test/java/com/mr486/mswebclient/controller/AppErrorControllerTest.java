package com.mr486.mswebclient.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AppErrorControllerTest {

    private final AppErrorController controller = new AppErrorController();
    private final HttpServletRequest request = mock(HttpServletRequest.class);

    // Prépare une requête portant le code d'erreur fourni.
    private void avecStatut(Object statut) {
        when(request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE)).thenReturn(statut);
    }

    @Test
    void handleError_retourneLaPage404PourUnStatut404() {
        avecStatut(404);

        assertThat(controller.handleError(request)).isEqualTo("errors/error-404");
    }

    @Test
    void handleError_retourneLaPage500PourUnStatut500() {
        avecStatut(500);

        assertThat(controller.handleError(request)).isEqualTo("errors/error-500");
    }

    @Test
    void handleError_retourneLaPageGeneriquePourUnAutreStatut() {
        avecStatut(403);

        assertThat(controller.handleError(request)).isEqualTo("errors/error");
    }

    @Test
    void handleError_retourneLaPageGeneriqueSansStatut() {
        avecStatut(null);

        assertThat(controller.handleError(request)).isEqualTo("errors/error");
    }
}
