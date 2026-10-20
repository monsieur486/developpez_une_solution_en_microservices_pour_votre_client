package com.mr486.msrisque.controller;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mr486.commun.exception.AppelRefuseException;
import com.mr486.commun.exception.ServiceIndisponibleException;
import com.mr486.msrisque.exception.PatientIntrouvableException;
import com.mr486.msrisque.service.EvaluationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(EvaluationController.class)
@DisplayName("API REST de l'évaluation du risque")
class EvaluationControllerTest {
    private static final Long PATIENT_EN_DANGER = 3L;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EvaluationService evaluationService;

    // Données de santé : l'évaluation ne doit pas répondre sans identifiants.
    @Test
    @DisplayName("un appel sans identifiants est refusé en 401")
    void evaluation_sansIdentifiants_renvoie401() throws Exception {
        mockMvc.perform(get("/evaluation/{id}", PATIENT_EN_DANGER))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(evaluationService);
    }

    // Même format que les autres API : le niveau voyage dans un objet JSON, avec le patient évalué.
    @Test
    @WithMockUser
    @DisplayName("le niveau est renvoyé en JSON avec l'identifiant du patient, espaces compris")
    void evaluation_renvoieLePatientEtSonNiveauEnJson() throws Exception {
        when(evaluationService.evaluationDuRisque(PATIENT_EN_DANGER)).thenReturn("In Danger");

        mockMvc.perform(get("/evaluation/{id}", PATIENT_EN_DANGER).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.patientId").value(PATIENT_EN_DANGER))
                .andExpect(jsonPath("$.niveau").value("In Danger"));
    }

    // Non-régression : quand le résultat était en texte brut, un client qui demandait du JSON
    // recevait 406 au lieu de l'erreur, et un client qui demandait du texte, 500 sans corps.
    @Test
    @WithMockUser
    @DisplayName("un client qui demande du JSON reçoit bien l'erreur 404, et non 406")
    void evaluation_patientInconnuDemandeEnJson_renvoie404Json() throws Exception {
        when(evaluationService.evaluationDuRisque(99L))
                .thenThrow(new PatientIntrouvableException(99L, new IllegalStateException("404")));

        mockMvc.perform(get("/evaluation/{id}", 99L).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.messages[0]").value("Aucun patient avec l'id: 99"));
    }

    @Test
    @WithMockUser
    @DisplayName("un patient inconnu donne un 404 JSON qui nomme le patient")
    void evaluation_patientInconnu_renvoie404() throws Exception {
        when(evaluationService.evaluationDuRisque(99L))
                .thenThrow(new PatientIntrouvableException(99L, new IllegalStateException("404")));

        mockMvc.perform(get("/evaluation/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.messages[0]").value("Aucun patient avec l'id: 99"));
    }

    // Une panne en aval ne doit pas se faire passer pour un patient introuvable.
    @Test
    @WithMockUser
    @DisplayName("un service en aval injoignable donne un 503")
    void evaluation_serviceInjoignable_renvoie503() throws Exception {
        when(evaluationService.evaluationDuRisque(PATIENT_EN_DANGER))
                .thenThrow(new ServiceIndisponibleException("ms-notes", new IllegalStateException("arrêté")));

        mockMvc.perform(get("/evaluation/{id}", PATIENT_EN_DANGER))
                .andExpect(status().isServiceUnavailable());
    }

    // Un refus en aval trahit un défaut de configuration de ms-risque, pas une faute du client :
    // le statut 502 le désigne, sans exposer le code reçu ni l'identité du service.
    @Test
    @WithMockUser
    @DisplayName("un service en aval qui refuse l'appel donne un 502 au message générique")
    void evaluation_appelRefuse_renvoie502() throws Exception {
        when(evaluationService.evaluationDuRisque(PATIENT_EN_DANGER))
                .thenThrow(new AppelRefuseException("ms-notes", 403, new IllegalStateException("403")));

        mockMvc.perform(get("/evaluation/{id}", PATIENT_EN_DANGER))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.messages[0]")
                        .value("Un service interne a refusé la requête."));
    }
}
