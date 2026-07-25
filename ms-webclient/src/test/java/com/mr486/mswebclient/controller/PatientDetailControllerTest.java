package com.mr486.mswebclient.controller;

import com.mr486.mswebclient.dto.ErrorMessage;
import com.mr486.mswebclient.dto.Patient;
import com.mr486.mswebclient.dto.PatientForm;
import com.mr486.mswebclient.service.PatientService;
import com.mr486.mswebclient.tools.ErrorResponseTools;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PatientDetailControllerTest {

    private final PatientService patientService = mock(PatientService.class);
    private final ErrorResponseTools errorResponseTools = mock(ErrorResponseTools.class);
    private final PatientDetailController controller =
            new PatientDetailController(patientService, errorResponseTools);

    private final Model model = new ExtendedModelMap();

    @Test
    void patientDetail_remplitLeModeleEtRetourneLaFiche() {
        Patient patient = new Patient();
        when(patientService.getPatientById(7L)).thenReturn(patient);

        String vue = controller.patientDetail(7L, model);

        assertThat(vue).isEqualTo("patients/patient-detail");
        assertThat(model.getAttribute("patient")).isEqualTo(patient);
    }

    @Test
    void patientDetail_afficheLErreurQuandLeServiceEchoue() {
        when(patientService.getPatientById(7L)).thenThrow(new RuntimeException("boom"));
        ErrorMessage erreur = new ErrorMessage(503, "ms-patients ne répond pas.");
        when(errorResponseTools.getErrorMessage(anyString(), anyString())).thenReturn(erreur);

        String vue = controller.patientDetail(7L, model);

        assertThat(vue).isEqualTo("patients/patient-detail");
        assertThat(model.getAttribute("errorMessage")).isEqualTo(erreur);
    }

    @Test
    void updatePatientForm_preRemplitLeFormulaire() {
        Patient patient = new Patient();
        when(patientService.getPatientById(7L)).thenReturn(patient);

        String vue = controller.updatePatientForm(7L, model);

        assertThat(vue).isEqualTo("patients/patient-update");
        assertThat(model.getAttribute("patient")).isEqualTo(patient);
        assertThat(model.getAttribute("id")).isEqualTo(7L);
    }

    @Test
    void updatePatientForm_afficheLErreurQuandLeServiceEchoue() {
        when(patientService.getPatientById(7L)).thenThrow(new RuntimeException("boom"));
        ErrorMessage erreur = new ErrorMessage(503, "ms-patients ne répond pas.");
        when(errorResponseTools.getErrorMessage(anyString(), anyString())).thenReturn(erreur);

        String vue = controller.updatePatientForm(7L, model);

        assertThat(vue).isEqualTo("patients/patient-update");
        assertThat(model.getAttribute("errorMessage")).isEqualTo(erreur);
        assertThat(model.getAttribute("id")).isEqualTo(7L);
    }

    @Test
    void updatePatient_redirigeVersLaFicheApresMiseAJour() {
        String vue = controller.updatePatient(7L, new PatientForm(), model);

        assertThat(vue).isEqualTo("redirect:/app/patients/7");
    }

    @Test
    void updatePatient_reafficheLeFormulaireQuandLaMiseAJourEchoue() {
        PatientForm form = new PatientForm();
        doThrow(new RuntimeException("boom"))
                .when(patientService).updatePatient(eq(7L), any(PatientForm.class));
        ErrorMessage erreur = new ErrorMessage(503, "ms-patients ne répond pas.");
        when(errorResponseTools.getErrorMessage(anyString(), anyString())).thenReturn(erreur);

        String vue = controller.updatePatient(7L, form, model);

        assertThat(vue).isEqualTo("patients/patient-update");
        assertThat(model.getAttribute("patient")).isEqualTo(form);
        assertThat(model.getAttribute("errorMessage")).isEqualTo(erreur);
    }
}
