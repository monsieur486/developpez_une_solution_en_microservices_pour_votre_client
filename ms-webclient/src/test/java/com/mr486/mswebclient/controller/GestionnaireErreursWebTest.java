package com.mr486.mswebclient.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.mr486.commun.exception.DuplicateException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

@DisplayName("Traduction des erreurs en pages pour l'interface web")
class GestionnaireErreursWebTest {
    private final GestionnaireErreursWeb gestionnaire = new GestionnaireErreursWeb();

    @Test
    @DisplayName("un doublon affiche la page d'erreur avec le message, sans proposer de réessayer")
    void doublon_afficheLaPageErreurNonReessayable() {
        Model model = new ExtendedModelMap();

        String vue = gestionnaire.doublon(new DuplicateException("La ressource existe déjà."), model);

        assertThat(vue).isEqualTo("erreur");
        assertThat(model.getAttribute("titre")).isEqualTo("Enregistrement déjà existant");
        assertThat(model.getAttribute("message")).isEqualTo("La ressource existe déjà.");
        assertThat(model.getAttribute("reessayable")).isEqualTo(false);
    }
}
