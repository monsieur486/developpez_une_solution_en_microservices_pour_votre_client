package com.mr486.mswebclient.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.mr486.commun.exception.AppelRefuseException;
import com.mr486.commun.exception.DuplicateException;
import com.mr486.commun.exception.ResourceNotFoundException;
import com.mr486.commun.exception.ServiceIndisponibleException;
import com.mr486.mswebclient.configuration.SecurityConfiguration;
import com.mr486.mswebclient.mapper.PatientFormMapper;
import com.mr486.mswebclient.service.NoteApiService;
import com.mr486.mswebclient.service.PatientApiService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

// La fiche patient sert de point d'entrée : c'est le statut HTTP rendu par le gestionnaire
// qui est vérifié, quelle que soit la façon dont il est produit (annotation, ResponseEntity…).
@WebMvcTest(PatientDetailController.class)
@Import({SecurityConfiguration.class, PatientFormMapper.class})
@WithMockUser
@DisplayName("Statuts HTTP des pages d'erreur de l'interface web")
class GestionnaireErreursWebMvcTest {
    private static final String FICHE = "/app/patients/42";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PatientApiService patientApiService;

    @MockitoBean
    private NoteApiService noteApiService;

    @Test
    @DisplayName("un doublon répond par le statut HTTP 409")
    void doublon_repondConflit() throws Exception {
        when(patientApiService.recupereLePatient(42L))
                .thenThrow(new DuplicateException("La ressource existe déjà."));

        mockMvc.perform(get(FICHE))
                .andExpect(status().isConflict())
                .andExpect(view().name("erreur"));
    }

    @Test
    @DisplayName("une ressource introuvable répond par le statut HTTP 404")
    void ressourceIntrouvable_repondNonTrouve() throws Exception {
        when(patientApiService.recupereLePatient(42L))
                .thenThrow(new ResourceNotFoundException("Aucun patient avec l'id: 42"));

        mockMvc.perform(get(FICHE))
                .andExpect(status().isNotFound())
                .andExpect(model().attribute("message", "Aucun patient avec l'id: 42"));
    }

    @Test
    @DisplayName("un service injoignable répond 503 et propose de réessayer")
    void serviceIndisponible_repondIndisponibleEtReessayable() throws Exception {
        when(patientApiService.recupereLePatient(42L))
                .thenThrow(new ServiceIndisponibleException("ms-patients", new IllegalStateException()));

        mockMvc.perform(get(FICHE))
                .andExpect(status().isServiceUnavailable())
                .andExpect(model().attribute("reessayable", true));
    }

    @Test
    @DisplayName("un appel refusé (400, 401, 403) répond 500 sans parler d'indisponibilité ni proposer de réessayer")
    void appelRefuse_repondErreurInterneNonReessayable() throws Exception {
        when(patientApiService.recupereLePatient(42L))
                .thenThrow(new AppelRefuseException("ms-patients", 403, new IllegalStateException()));

        mockMvc.perform(get(FICHE))
                .andExpect(status().isInternalServerError())
                .andExpect(model().attribute("titre", "Erreur inattendue"))
                .andExpect(model().attribute("reessayable", false));
    }
}
