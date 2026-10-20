package com.mr486.mswebclient.controller;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.mr486.commun.dto.NoteDto;
import com.mr486.commun.dto.PatientDto;
import com.mr486.commun.exception.ServiceIndisponibleException;
import com.mr486.mswebclient.configuration.SecurityConfiguration;
import com.mr486.mswebclient.mapper.PatientFormMapper;
import com.mr486.mswebclient.service.NoteApiService;
import com.mr486.mswebclient.service.PatientApiService;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PatientDetailController.class)
@Import({SecurityConfiguration.class, PatientFormMapper.class})
@DisplayName("Fiche d'un patient à travers Spring MVC : notes et modification")
class PatientDetailControllerMvcTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PatientApiService patientApiService;

    @MockitoBean
    private NoteApiService noteApiService;

    // Un historique sans date ne permet pas au praticien de suivre l'évolution du patient.
    @Test
    @WithMockUser
    @DisplayName("la fiche affiche chaque note avec sa date d'enregistrement")
    void fiche_afficheLesNotesAvecLeurDate() throws Exception {
        when(patientApiService.recupereLePatient(1L)).thenReturn(unPatient());
        // Midi UTC : la date affichée reste le 20/10/2026 quel que soit le fuseau du poste.
        when(noteApiService.listeLesNotes(1L)).thenReturn(List.of(NoteDto.builder()
                .content("Le patient déclare qu'il fume depuis peu")
                .createdDate(Instant.parse("2026-10-20T12:00:00Z"))
                .build()));

        mockMvc.perform(get("/app/patients/1"))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("20/10/2026")))
                .andExpect(content().string(Matchers.containsString("fume depuis peu")));
    }

    // Isolement des pannes : sans ms-notes, le praticien doit encore pouvoir consulter le patient.
    @Test
    @WithMockUser
    @DisplayName("une panne de ms-notes laisse la fiche s'afficher, avec un avertissement à la place des notes")
    void fiche_notesIndisponibles_afficheLePatientEtUnAvertissement() throws Exception {
        when(patientApiService.recupereLePatient(1L)).thenReturn(unPatient());
        when(noteApiService.listeLesNotes(1L))
                .thenThrow(new ServiceIndisponibleException("ms-notes", new IllegalStateException("arrêté")));

        mockMvc.perform(get("/app/patients/1"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("notesIndisponibles", true))
                .andExpect(content().string(Matchers.containsString("TestInDanger")))
                .andExpect(content().string(Matchers.containsString("momentanément indisponibles")))
                .andExpect(content().string(Matchers.not(Matchers.containsString("Ajouter une note"))));
    }

    @Test
    @WithMockUser
    @DisplayName("une modification invalide réaffiche le formulaire de ce patient, sans appel à l'API")
    void modificationInvalide_reafficheLeFormulaireSansAppelALApi() throws Exception {
        mockMvc.perform(post("/app/patients/42/update").with(csrf())
                        .param("firstName", "")
                        .param("lastName", "TestNone")
                        .param("birthDate", "1966-12-31")
                        .param("gender", "F"))
                .andExpect(status().isOk())
                .andExpect(view().name("patient-update"))
                .andExpect(model().attribute("patientId", 42L))
                .andExpect(model().attributeHasFieldErrors("patient", "firstName"));

        verifyNoInteractions(patientApiService);
    }

    // Patient tel que l'API des patients le renvoie.
    private PatientDto unPatient() {
        return PatientDto.builder().id(1L).firstName("Test").lastName("TestInDanger")
                .birthDate(LocalDate.of(2004, 6, 18)).gender("M").build();
    }
}
