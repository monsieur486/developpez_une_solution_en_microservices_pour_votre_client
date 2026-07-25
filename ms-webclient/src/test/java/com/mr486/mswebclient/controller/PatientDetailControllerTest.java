package com.mr486.mswebclient.controller;

import com.mr486.mswebclient.dto.ErrorMessage;
import com.mr486.mswebclient.dto.Patient;
import com.mr486.mswebclient.dto.PatientForm;
import com.mr486.mswebclient.exception.GatewayException;
import com.mr486.mswebclient.service.PatientService;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PatientDetailControllerTest {

    private static final GatewayException PANNE =
            new GatewayException(new ErrorMessage(503, "ms-patients ne répond pas."));

    private final PatientService patientService = mock(PatientService.class);
    private final PatientDetailController controller = new PatientDetailController(patientService);

    private final Model model = new ExtendedModelMap();

    @Test
    void patientDetail_remplitLeModeleEtRetourneLaFiche() {
        Patient patient = new Patient();
        when(patientService.getPatientById(7L)).thenReturn(Mono.just(patient));

        String vue = controller.patientDetail(7L, model).block();

        assertThat(vue).isEqualTo("patients/patient-detail");
        assertThat(model.getAttribute("patient")).isEqualTo(patient);
    }

    @Test
    void patientDetail_afficheLErreurQuandLeServiceEchoue() {
        when(patientService.getPatientById(7L)).thenReturn(Mono.error(PANNE));

        String vue = controller.patientDetail(7L, model).block();

        assertThat(vue).isEqualTo("patients/patient-detail");
        assertThat(model.getAttribute("errorMessage")).isEqualTo(PANNE.getErrorMessage());
    }

    @Test
    void updatePatientForm_preRemplitLeFormulaire() {
        Patient patient = new Patient();
        when(patientService.getPatientById(7L)).thenReturn(Mono.just(patient));

        String vue = controller.updatePatientForm(7L, model).block();

        assertThat(vue).isEqualTo("patients/patient-update");
        assertThat(model.getAttribute("patient")).isEqualTo(patient);
        assertThat(model.getAttribute("id")).isEqualTo(7L);
    }

    @Test
    void updatePatientForm_afficheLErreurQuandLeServiceEchoue() {
        when(patientService.getPatientById(7L)).thenReturn(Mono.error(PANNE));

        String vue = controller.updatePatientForm(7L, model).block();

        assertThat(vue).isEqualTo("patients/patient-update");
        assertThat(model.getAttribute("errorMessage")).isEqualTo(PANNE.getErrorMessage());
        assertThat(model.getAttribute("id")).isEqualTo(7L);
    }

    @Test
    void updatePatient_redirigeVersLaFicheApresMiseAJour() {
        when(patientService.updatePatient(eq(7L), any(PatientForm.class))).thenReturn(Mono.empty());

        String vue = controller.updatePatient(7L, new PatientForm(), model).block();

        assertThat(vue).isEqualTo("redirect:/app/patients/7");
    }

    @Test
    void updatePatient_reafficheLeFormulaireQuandLaMiseAJourEchoue() {
        PatientForm form = new PatientForm();
        when(patientService.updatePatient(eq(7L), any(PatientForm.class))).thenReturn(Mono.error(PANNE));

        String vue = controller.updatePatient(7L, form, model).block();

        assertThat(vue).isEqualTo("patients/patient-update");
        assertThat(model.getAttribute("patient")).isEqualTo(form);
        assertThat(model.getAttribute("errorMessage")).isEqualTo(PANNE.getErrorMessage());
    }
}
