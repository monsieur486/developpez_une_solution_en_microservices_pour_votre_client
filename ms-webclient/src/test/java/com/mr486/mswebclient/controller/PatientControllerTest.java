package com.mr486.mswebclient.controller;

import com.mr486.mswebclient.dto.ErrorMessage;
import com.mr486.mswebclient.dto.Patient;
import com.mr486.mswebclient.dto.PatientForm;
import com.mr486.mswebclient.exception.GatewayException;
import com.mr486.mswebclient.service.PatientService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PatientControllerTest {

    private static final GatewayException PANNE =
            new GatewayException(new ErrorMessage(503, "ms-patients ne répond pas."));

    private final PatientService patientService = mock(PatientService.class);
    private final PatientController controller = new PatientController(patientService);

    private final Model model = new ExtendedModelMap();

    @Test
    void patients_remplitLeModeleEtRetourneLaListe() {
        List<Patient> attendus = List.of(new Patient());
        when(patientService.getPatients()).thenReturn(Flux.fromIterable(attendus));

        String vue = controller.patients(model).block();

        assertThat(vue).isEqualTo("patients/patients");
        assertThat(model.getAttribute("patients")).isEqualTo(attendus);
    }

    @Test
    void patients_afficheLErreurQuandLeServiceEchoue() {
        when(patientService.getPatients()).thenReturn(Flux.error(PANNE));

        String vue = controller.patients(model).block();

        assertThat(vue).isEqualTo("patients/patients");
        assertThat(model.getAttribute("errorMessage")).isEqualTo(PANNE.getErrorMessage());
    }

    @Test
    void showCreatePatientForm_prepareUnFormulaireVierge() {
        String vue = controller.showCreatePatientForm(model);

        assertThat(vue).isEqualTo("patients/patient-ajout");
        assertThat(model.getAttribute("patient")).isInstanceOf(PatientForm.class);
    }

    @Test
    void ajoutPatientPost_redirigeVersLaListeApresCreation() {
        when(patientService.createPatient(any(PatientForm.class))).thenReturn(Mono.empty());

        String vue = controller.ajoutPatientPost(new PatientForm(), model).block();

        assertThat(vue).isEqualTo("redirect:/app/patients");
    }

    @Test
    void ajoutPatientPost_reafficheLeFormulaireQuandLaCreationEchoue() {
        PatientForm form = new PatientForm();
        when(patientService.createPatient(any(PatientForm.class))).thenReturn(Mono.error(PANNE));

        String vue = controller.ajoutPatientPost(form, model).block();

        assertThat(vue).isEqualTo("patients/patient-ajout");
        assertThat(model.getAttribute("patient")).isEqualTo(form);
        assertThat(model.getAttribute("errorMessage")).isEqualTo(PANNE.getErrorMessage());
    }
}
