package com.mr486.mswebclient.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.mr486.commun.dto.NoteForm;
import com.mr486.mswebclient.configuration.ConstantesToast;
import com.mr486.mswebclient.configuration.SecurityConfiguration;
import com.mr486.mswebclient.service.NoteApiService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(NotesController.class)
@Import(SecurityConfiguration.class)
@DisplayName("Ajout d'une note à travers Spring MVC : routes, validation et sécurité")
class NotesControllerMvcTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NoteApiService noteApiService;

    @Test
    @WithMockUser
    @DisplayName("le formulaire d'ajout s'ouvre vide pour le patient de l'URL")
    void formulaireDAjout_estVidePourLePatient() throws Exception {
        mockMvc.perform(get("/app/patients/1/notes/ajout"))
                .andExpect(status().isOk())
                .andExpect(view().name("note-ajout"))
                .andExpect(model().attribute("patientId", 1L))
                .andExpect(model().attributeExists("note"));
    }

    @Test
    @WithMockUser
    @DisplayName("une note vide est réaffichée avec son erreur, sans appel à l'API")
    void noteVide_estReafficheeSansAppelALApi() throws Exception {
        mockMvc.perform(post("/app/patients/1/notes").with(csrf())
                        .param("content", "   "))
                .andExpect(status().isOk())
                .andExpect(view().name("note-ajout"))
                .andExpect(model().attribute("patientId", 1L))
                .andExpect(model().attributeHasFieldErrors("note", "content"));

        verifyNoInteractions(noteApiService);
    }

    @Test
    @WithMockUser
    @DisplayName("une note valide est transmise, puis la fiche s'ouvre avec une confirmation")
    void noteValide_estTransmiseEtRedirigeVersLaFiche() throws Exception {
        mockMvc.perform(post("/app/patients/1/notes").with(csrf())
                        .param("content", "Poids en hausse"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/app/patients/1"))
                .andExpect(flash().attribute(ConstantesToast.CLE_NIVEAU, ConstantesToast.NIVEAU_INFO))
                .andExpect(flash().attribute(ConstantesToast.CLE_MESSAGE, "Note ajoutée au dossier."));

        ArgumentCaptor<NoteForm> note = ArgumentCaptor.forClass(NoteForm.class);
        verify(noteApiService).ajouteUneNote(eq(1L), note.capture());
        assertThat(note.getValue().getContent()).isEqualTo("Poids en hausse");
    }

    @Test
    @WithMockUser
    @DisplayName("un envoi sans jeton CSRF est refusé, sans appel à l'API")
    void envoiSansJetonCsrf_estRefuse() throws Exception {
        mockMvc.perform(post("/app/patients/1/notes")
                        .param("content", "Poids en hausse"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(noteApiService);
    }
}
