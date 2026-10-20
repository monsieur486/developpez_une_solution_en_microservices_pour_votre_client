package com.mr486.mswebclient.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.mr486.commun.dto.PatientForm;
import com.mr486.mswebclient.configuration.SecurityConfiguration;
import com.mr486.mswebclient.service.PatientApiService;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PatientsController.class)
@Import(SecurityConfiguration.class)
@DisplayName("Ajout d'un patient à travers Spring MVC : routes, validation et sécurité")
class PatientsControllerMvcTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PatientApiService patientApiService;

    @Test
    @DisplayName("un visiteur non connecté est renvoyé vers la page de connexion")
    void visiteurAnonyme_estRedirigeVersLaConnexion() throws Exception {
        mockMvc.perform(get("/app/patients"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));

        verifyNoInteractions(patientApiService);
    }

    @Test
    @WithMockUser
    @DisplayName("un formulaire invalide est réaffiché avec ses erreurs, sans appel à l'API")
    void formulaireInvalide_estReafficheSansAppelALApi() throws Exception {
        mockMvc.perform(post("/app/patients/ajout").with(csrf())
                        .param("firstName", "")
                        .param("lastName", "")
                        .param("birthDate", "2999-01-01")
                        .param("gender", "X"))
                .andExpect(status().isOk())
                .andExpect(view().name("patient-ajout"))
                .andExpect(model().attributeHasFieldErrors(
                        "patient", "firstName", "lastName", "birthDate", "gender"));

        verifyNoInteractions(patientApiService);
    }

    @Test
    @WithMockUser
    @DisplayName("un envoi sans jeton CSRF est refusé, sans appel à l'API")
    void envoiSansJetonCsrf_estRefuse() throws Exception {
        mockMvc.perform(post("/app/patients/ajout")
                        .param("firstName", "Test")
                        .param("lastName", "TestNone")
                        .param("birthDate", "1966-12-31")
                        .param("gender", "F"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(patientApiService);
    }

    @Test
    @WithMockUser
    @DisplayName("un formulaire valide transmet à l'API les champs saisis, date comprise")
    void formulaireValide_transmetLesChampsSaisis() throws Exception {
        mockMvc.perform(post("/app/patients/ajout").with(csrf())
                        .param("firstName", "Test")
                        .param("lastName", "TestNone")
                        .param("birthDate", "1966-12-31")
                        .param("gender", "F"))
                .andExpect(redirectedUrl("/app/patients"));

        ArgumentCaptor<PatientForm> envoye = ArgumentCaptor.forClass(PatientForm.class);
        verify(patientApiService).creeLePatient(envoye.capture());
        assertThat(envoye.getValue().getLastName()).isEqualTo("TestNone");
        assertThat(envoye.getValue().getBirthDate()).isEqualTo(LocalDate.of(1966, 12, 31));
        assertThat(envoye.getValue().getGender()).isEqualTo("F");
    }

    // Un navigateur envoie les champs vides en "" : ils doivent arriver null à l'API.
    @Test
    @WithMockUser
    @DisplayName("les champs optionnels laissés vides sont transmis à null, les textes sans espaces")
    void champsVides_sontTransmisANull() throws Exception {
        mockMvc.perform(post("/app/patients/ajout").with(csrf())
                        .param("firstName", " Test ")
                        .param("lastName", "TestNone")
                        .param("birthDate", "1966-12-31")
                        .param("gender", "F")
                        .param("postalAddress", "")
                        .param("phoneNumber", "   "))
                .andExpect(redirectedUrl("/app/patients"));

        ArgumentCaptor<PatientForm> envoye = ArgumentCaptor.forClass(PatientForm.class);
        verify(patientApiService).creeLePatient(envoye.capture());
        assertThat(envoye.getValue().getFirstName()).isEqualTo("Test");
        assertThat(envoye.getValue().getPostalAddress()).isNull();
        assertThat(envoye.getValue().getPhoneNumber()).isNull();
    }
}
