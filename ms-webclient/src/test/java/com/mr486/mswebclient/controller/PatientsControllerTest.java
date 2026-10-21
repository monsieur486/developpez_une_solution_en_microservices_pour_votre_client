package com.mr486.mswebclient.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;

import com.mr486.commun.dto.PatientForm;
import com.mr486.commun.exception.DuplicateException;
import com.mr486.mswebclient.configuration.ConstantesToast;
import com.mr486.mswebclient.service.PatientApiService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

@ExtendWith(MockitoExtension.class)
@DisplayName("Ajout d'un patient depuis l'interface web")
class PatientsControllerTest {
    @Mock
    private PatientApiService patientApiService;

    @InjectMocks
    private PatientsController controleur;

    @Test
    @DisplayName("un patient en doublon réaffiche le formulaire avec un toast d'avertissement")
    void ajoutEnDoublon_reafficheLeFormulaireAvecToast() {
        PatientForm patient = new PatientForm();
        doThrow(new DuplicateException("Ce patient existe déjà."))
                .when(patientApiService).creeLePatient(patient);
        Model model = new ExtendedModelMap();

        String vue = controleur.ajoutPatientSubmit(
                patient, new BeanPropertyBindingResult(patient, "patient"),
                model, new RedirectAttributesModelMap());

        assertThat(vue).isEqualTo("patient-ajout");
        assertThat(model.getAttribute(ConstantesToast.CLE_NIVEAU))
                .isEqualTo(ConstantesToast.NIVEAU_AVERTISSEMENT);
        assertThat(model.getAttribute(ConstantesToast.CLE_MESSAGE))
                .isEqualTo("Ce patient existe déjà.");
    }

    @Test
    @DisplayName("un ajout abouti redirige vers le tableau de bord avec un toast de confirmation")
    void ajoutAbouti_redirigeAvecToast() {
        PatientForm patient = new PatientForm();
        RedirectAttributes redirection = new RedirectAttributesModelMap();

        String vue = controleur.ajoutPatientSubmit(
                patient, new BeanPropertyBindingResult(patient, "patient"),
                new ExtendedModelMap(), redirection);

        assertThat(vue).isEqualTo("redirect:/app/patients");
        assertThat(redirection.getFlashAttributes().get(ConstantesToast.CLE_MESSAGE))
                .isEqualTo("Patient créé.");
    }
}
