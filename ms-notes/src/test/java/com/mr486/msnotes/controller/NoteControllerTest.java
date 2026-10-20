package com.mr486.msnotes.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mr486.commun.dto.NoteForm;
import com.mr486.msnotes.mapper.NoteMapper;
import com.mr486.msnotes.model.Note;
import com.mr486.msnotes.service.NoteService;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(NoteController.class)
@Import(NoteMapper.class)
@DisplayName("API REST des notes")
class NoteControllerTest {
    private static final Long PATIENT_EN_DANGER = 3L;

    private static final Instant ENREGISTREMENT = Instant.parse("2026-10-20T16:13:39Z");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NoteService noteService;

    // Données de santé : aucune route ne doit répondre sans identifiants.
    @Test
    @DisplayName("un appel sans identifiants est refusé en 401")
    void getNotes_sansIdentifiants_renvoie401() throws Exception {
        mockMvc.perform(get("/patients/{id}/notes", PATIENT_EN_DANGER))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(noteService);
    }

    @Test
    @WithMockUser
    @DisplayName("l'historique renvoie le texte et la date de chaque note")
    void getNotes_renvoieTexteEtDate() throws Exception {
        when(noteService.findByPatientId(PATIENT_EN_DANGER))
                .thenReturn(List.of(uneNote("Le patient déclare qu'il fume depuis peu")));

        mockMvc.perform(get("/patients/{id}/notes", PATIENT_EN_DANGER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content").value("Le patient déclare qu'il fume depuis peu"))
                .andExpect(jsonPath("$[0].createdDate").value("2026-10-20T16:13:39Z"));
    }

    @Test
    @WithMockUser
    @DisplayName("une note faite d'espaces est refusée en 400 sans atteindre le service")
    void addNote_vide_renvoie400() throws Exception {
        mockMvc.perform(post("/patients/{id}/notes", PATIENT_EN_DANGER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.messages[0]").value("content: Le contenu de la note est obligatoire"));

        verifyNoInteractions(noteService);
    }

    // La borne protège la base et chaque affichage de la fiche d'un texte démesuré.
    @Test
    @WithMockUser
    @DisplayName("une note de plus de 5000 caractères est refusée en 400")
    void addNote_tropLongue_renvoie400() throws Exception {
        mockMvc.perform(post("/patients/{id}/notes", PATIENT_EN_DANGER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"" + "a".repeat(5001) + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.messages[0]").value("content: La note ne doit pas dépasser 5000 caractères"));

        verifyNoInteractions(noteService);
    }

    @Test
    @WithMockUser
    @DisplayName("une note valide est créée en 201 avec sa date d'enregistrement")
    void addNote_valide_renvoie201() throws Exception {
        when(noteService.save(eq(PATIENT_EN_DANGER), any(NoteForm.class)))
                .thenReturn(uneNote("Poids en hausse"));

        mockMvc.perform(post("/patients/{id}/notes", PATIENT_EN_DANGER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"Poids en hausse\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("Poids en hausse"))
                .andExpect(jsonPath("$.createdDate").value("2026-10-20T16:13:39Z"));
    }

    // Note telle que le service la renvoie après enregistrement.
    private Note uneNote(String contenu) {
        return Note.builder().id("n1").patientId(PATIENT_EN_DANGER).content(contenu)
                .createdDate(ENREGISTREMENT).build();
    }
}
