package com.mr486.mswebclient.controller;

import com.mr486.mswebclient.dto.ErrorMessage;
import com.mr486.mswebclient.dto.Patient;
import com.mr486.mswebclient.dto.PatientForm;
import com.mr486.mswebclient.service.PatientService;
import com.mr486.mswebclient.tools.ErrorResponseTools;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PatientControllerTest {

    private final PatientService patientService = mock(PatientService.class);
    private final ErrorResponseTools errorResponseTools = mock(ErrorResponseTools.class);
    private final PatientController controller = new PatientController(patientService, errorResponseTools);

    private final Model model = new ExtendedModelMap();

    @Test
    void patients_remplitLeModeleEtRetourneLaListe() {
        List<Patient> attendus = List.of(new Patient());
        when(patientService.getPatients()).thenReturn(attendus);

        String vue = controller.patients(model);

        assertThat(vue).isEqualTo("patients/patients");
        assertThat(model.getAttribute("patients")).isEqualTo(attendus);
    }

    @Test
    void patients_afficheLErreurQuandLeServiceEchoue() {
        when(patientService.getPatients()).thenThrow(new RuntimeException("boom"));
        ErrorMessage erreur = new ErrorMessage(503, "ms-patients ne répond pas.");
        when(errorResponseTools.getErrorMessage(anyString(), anyString())).thenReturn(erreur);

        String vue = controller.patients(model);

        assertThat(vue).isEqualTo("patients/patients");
        assertThat(model.getAttribute("errorMessage")).isEqualTo(erreur);
    }

    @Test
    void showCreatePatientForm_prepareUnFormulaireVierge() {
        String vue = controller.showCreatePatientForm(model);

        assertThat(vue).isEqualTo("patients/patient-ajout");
        assertThat(model.getAttribute("patient")).isInstanceOf(PatientForm.class);
    }

    @Test
    void ajoutPatientPost_redirigeVersLaListeApresCreation() {
        String vue = controller.ajoutPatientPost(new PatientForm(), model);

        assertThat(vue).isEqualTo("redirect:/app/patients");
    }

    @Test
    void ajoutPatientPost_reafficheLeFormulaireQuandLaCreationEchoue() {
        PatientForm form = new PatientForm();
        doThrow(new RuntimeException("boom")).when(patientService).createPatient(any(PatientForm.class));
        ErrorMessage erreur = new ErrorMessage(503, "ms-patients ne répond pas.");
        when(errorResponseTools.getErrorMessage(anyString(), anyString())).thenReturn(erreur);

        String vue = controller.ajoutPatientPost(form, model);

        assertThat(vue).isEqualTo("patients/patient-ajout");
        assertThat(model.getAttribute("patient")).isEqualTo(form);
        assertThat(model.getAttribute("errorMessage")).isEqualTo(erreur);
    }
}
