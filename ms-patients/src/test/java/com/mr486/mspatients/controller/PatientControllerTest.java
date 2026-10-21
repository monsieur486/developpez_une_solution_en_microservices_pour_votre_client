package com.mr486.mspatients.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mr486.commun.dto.PatientForm;
import com.mr486.mspatients.exception.PatientIntrouvableException;
import com.mr486.mspatients.mapper.PatientMapper;
import com.mr486.mspatients.model.Patient;
import com.mr486.mspatients.service.PatientService;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PatientController.class)
@Import(PatientMapper.class)
@DisplayName("API REST des patients")
class PatientControllerTest {
    private static final Long PATIENT_INCONNU = 999L;

    private static final String CORPS_VALIDE = """
            {"firstName":"Test","lastName":"TestNone","birthDate":"1966-12-31","gender":"F"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PatientService patientService;

    // Données de santé : aucune route ne doit répondre sans identifiants.
    @Test
    @DisplayName("un appel sans identifiants est refusé en 401")
    void getPatients_sansIdentifiants_renvoie401() throws Exception {
        mockMvc.perform(get("/patients"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(patientService);
    }

    @Test
    @WithMockUser
    @DisplayName("un identifiant inconnu renvoie 404 avec le message du service")
    void getPatient_inconnu_renvoie404() throws Exception {
        when(patientService.findById(PATIENT_INCONNU))
                .thenThrow(new PatientIntrouvableException(PATIENT_INCONNU));

        mockMvc.perform(get("/patients/{id}", PATIENT_INCONNU))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.messages[0]").value("Aucun patient avec l'id: 999"));
    }

    @Test
    @WithMockUser
    @DisplayName("un formulaire sans nom de famille est refusé en 400 sans atteindre le service")
    void createPatient_sansNom_renvoie400() throws Exception {
        mockMvc.perform(post("/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"Test","birthDate":"1966-12-31","gender":"F"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.messages[0]").value("lastName: Le nom de famille est obligatoire"));

        verifyNoInteractions(patientService);
    }

    // Une date future ou un genre inconnu fausseraient l'évaluation du risque au Sprint 3.
    @Test
    @WithMockUser
    @DisplayName("une date de naissance future est refusée en 400")
    void createPatient_naissanceFuture_renvoie400() throws Exception {
        mockMvc.perform(post("/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"Test","lastName":"TestNone","birthDate":"2999-01-01","gender":"F"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.messages[0]").value("birthDate: La date de naissance doit être dans le passé"));

        verifyNoInteractions(patientService);
    }

    @Test
    @WithMockUser
    @DisplayName("un genre autre que M ou F est refusé en 400")
    void createPatient_genreInconnu_renvoie400() throws Exception {
        mockMvc.perform(post("/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"Test","lastName":"TestNone","birthDate":"1966-12-31","gender":"X"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.messages[0]").value("gender: Le genre doit valoir M ou F"));

        verifyNoInteractions(patientService);
    }

    @Test
    @WithMockUser
    @DisplayName("un formulaire valide crée le patient et renvoie 201 avec son identifiant")
    void createPatient_valide_renvoie201() throws Exception {
        when(patientService.savePatient(any(PatientForm.class))).thenReturn(unPatientEnregistre());

        mockMvc.perform(post("/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CORPS_VALIDE))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.lastName").value("TestNone"))
                .andExpect(jsonPath("$.birthDate").value("1966-12-31"));
    }

    // Patient tel que le service le renvoie après enregistrement.
    private Patient unPatientEnregistre() {
        Patient patient = new Patient();
        patient.setId(1L);
        patient.setFirstName("Test");
        patient.setLastName("TestNone");
        patient.setBirthDate(LocalDate.of(1966, 12, 31));
        patient.setGender("F");
        return patient;
    }
}
