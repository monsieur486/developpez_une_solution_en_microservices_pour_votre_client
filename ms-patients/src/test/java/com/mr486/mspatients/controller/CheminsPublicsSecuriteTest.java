package com.mr486.mspatients.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mr486.mspatients.mapper.PatientMapper;
import com.mr486.mspatients.service.PatientService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springdoc.core.configuration.SpringDocConfiguration;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springdoc.webmvc.core.configuration.SpringDocWebMvcConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

// La tranche @WebMvcTest ne charge pas springdoc : on l'ajoute pour servir le vrai contrat OpenAPI.
@WebMvcTest(PatientController.class)
@Import(PatientMapper.class)
@ImportAutoConfiguration({
        SpringDocConfiguration.class,
        SpringDocConfigProperties.class,
        SpringDocWebMvcConfiguration.class})
@DisplayName("Chemins publics de la sécurité de l'API")
class CheminsPublicsSecuriteTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PatientService patientService;

    // La passerelle agrège les contrats OpenAPI sans identifiants : ce chemin doit rester ouvert.
    @Test
    @DisplayName("le contrat OpenAPI est servi sans identifiants, avec la gateway pour serveur")
    void getApiDocs_sansIdentifiants_renvoieLeContratAvecLaGatewayPourServeur() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("Gestion des patients API"))
                // Serveur préfixé par le nom du service : « Try it out » passe par la gateway,
                // qui retire ce préfixe, au lieu de viser le service en direct.
                .andExpect(jsonPath("$.servers[0].url").value("/ms-patients"));
    }
}
