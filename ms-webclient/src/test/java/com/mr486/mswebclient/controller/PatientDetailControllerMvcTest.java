package com.mr486.mswebclient.controller;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.mr486.mswebclient.configuration.SecurityConfiguration;
import com.mr486.mswebclient.service.PatientApiService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PatientDetailController.class)
@Import(SecurityConfiguration.class)
@DisplayName("Modification d'un patient à travers Spring MVC : validation")
class PatientDetailControllerMvcTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PatientApiService patientApiService;

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
}
