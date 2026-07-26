package com.mr486.mswebclient.controller;

import com.mr486.mswebclient.dto.ErrorMessage;
import com.mr486.mswebclient.dto.Note;
import com.mr486.mswebclient.dto.PageReponse;
import com.mr486.mswebclient.dto.Patient;
import com.mr486.mswebclient.dto.PatientForm;
import com.mr486.mswebclient.dto.Risque;
import com.mr486.mswebclient.exception.GatewayException;
import com.mr486.mswebclient.service.EvaluationService;
import com.mr486.mswebclient.service.NoteService;
import com.mr486.mswebclient.service.PatientService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
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

    private static final GatewayException PANNE_PATIENTS =
            new GatewayException(new ErrorMessage(503, "ms-patients ne répond pas."));
    private static final GatewayException PANNE_RISQUE =
            new GatewayException(new ErrorMessage(503, "ms-risque ne répond pas."));
    private static final GatewayException PANNE_NOTES =
            new GatewayException(new ErrorMessage(503, "ms-notes ne répond pas."));

    private final PatientService patientService = mock(PatientService.class);
    private final NoteService noteService = mock(NoteService.class);
    private final EvaluationService evaluationService = mock(EvaluationService.class);
    private final PatientDetailController controller =
            new PatientDetailController(patientService, noteService, evaluationService);

    private final Model model = new ExtendedModelMap();
    private final Patient patient = new Patient();
    private final PageReponse<Note> notes = PageReponse.<Note>builder()
            .content(List.of(new Note("fumeur"))).page(0).totalPages(1).totalElements(1).build();

    @BeforeEach
    void init() {
        patient.setId(3L);
    }

    @Test
    void patientDetail_afficheLaFicheCompleteAvecRisqueEtNotes() {
        when(patientService.getPatientById(3L)).thenReturn(Mono.just(patient));
        when(evaluationService.getEvaluationByPatientId(3L)).thenReturn(Mono.just(new Risque("In Danger")));
        when(noteService.getNotesPagines(3L, 0)).thenReturn(Mono.just(notes));

        String vue = controller.patientDetail(3L, 0, model).block();

        assertThat(vue).isEqualTo("patients/patient-detail");
        assertThat(model.getAttribute("patient")).isEqualTo(patient);
        assertThat(model.getAttribute("evaluation")).isEqualTo("In Danger");
        assertThat(model.getAttribute("notesPage")).isEqualTo(notes);
    }

    @Test
    void patientDetail_passeLaPageDeNotesDemandee() {
        when(patientService.getPatientById(3L)).thenReturn(Mono.just(patient));
        when(evaluationService.getEvaluationByPatientId(3L)).thenReturn(Mono.just(new Risque("None")));
        when(noteService.getNotesPagines(3L, 2)).thenReturn(Mono.just(notes));

        controller.patientDetail(3L, 2, model).block();

        assertThat(model.getAttribute("notesPage")).isEqualTo(notes);
    }

    @Test
    void patientDetail_afficheLErreurGlobaleQuandLePatientEstIndisponible() {
        when(patientService.getPatientById(3L)).thenReturn(Mono.error(PANNE_PATIENTS));

        String vue = controller.patientDetail(3L, 0, model).block();

        assertThat(vue).isEqualTo("patients/patient-detail");
        assertThat(model.getAttribute("errorMessage")).isEqualTo(PANNE_PATIENTS.getErrorMessage());
        assertThat(model.getAttribute("patient")).isNull();
    }

    @Test
    void patientDetail_afficheLaFicheAvecErreurDeSectionQuandLeRisqueEstIndisponible() {
        when(patientService.getPatientById(3L)).thenReturn(Mono.just(patient));
        when(evaluationService.getEvaluationByPatientId(3L)).thenReturn(Mono.error(PANNE_RISQUE));
        when(noteService.getNotesPagines(3L, 0)).thenReturn(Mono.just(notes));

        String vue = controller.patientDetail(3L, 0, model).block();

        assertThat(vue).isEqualTo("patients/patient-detail");
        assertThat(model.getAttribute("patient")).isEqualTo(patient);
        assertThat(model.getAttribute("erreurEvaluation")).isEqualTo(PANNE_RISQUE.getErrorMessage());
        assertThat(model.getAttribute("evaluation")).isNull();
        assertThat(model.getAttribute("notesPage")).isEqualTo(notes);
    }

    @Test
    void patientDetail_afficheLaFicheAvecErreurDeSectionQuandLesNotesSontIndisponibles() {
        when(patientService.getPatientById(3L)).thenReturn(Mono.just(patient));
        when(evaluationService.getEvaluationByPatientId(3L)).thenReturn(Mono.just(new Risque("In Danger")));
        when(noteService.getNotesPagines(3L, 0)).thenReturn(Mono.error(PANNE_NOTES));

        String vue = controller.patientDetail(3L, 0, model).block();

        assertThat(vue).isEqualTo("patients/patient-detail");
        assertThat(model.getAttribute("patient")).isEqualTo(patient);
        assertThat(model.getAttribute("evaluation")).isEqualTo("In Danger");
        assertThat(model.getAttribute("erreurNotes")).isEqualTo(PANNE_NOTES.getErrorMessage());
        assertThat(model.getAttribute("notesPage")).isNull();
    }

    @Test
    void updatePatientForm_preRemplitLeFormulaire() {
        when(patientService.getPatientById(3L)).thenReturn(Mono.just(patient));

        String vue = controller.updatePatientForm(3L, model).block();

        assertThat(vue).isEqualTo("patients/patient-update");
        assertThat(model.getAttribute("patient")).isEqualTo(patient);
        assertThat(model.getAttribute("id")).isEqualTo(3L);
    }

    @Test
    void updatePatientForm_afficheLErreurQuandLeServiceEchoue() {
        when(patientService.getPatientById(3L)).thenReturn(Mono.error(PANNE_PATIENTS));

        String vue = controller.updatePatientForm(3L, model).block();

        assertThat(vue).isEqualTo("patients/patient-update");
        assertThat(model.getAttribute("errorMessage")).isEqualTo(PANNE_PATIENTS.getErrorMessage());
    }

    @Test
    void updatePatient_redirigeVersLaFicheApresMiseAJour() {
        when(patientService.updatePatient(eq(3L), any(PatientForm.class))).thenReturn(Mono.empty());

        String vue = controller.updatePatient(3L, new PatientForm(), model).block();

        assertThat(vue).isEqualTo("redirect:/app/patients/3");
    }

    @Test
    void updatePatient_reafficheLeFormulaireQuandLaMiseAJourEchoue() {
        PatientForm form = new PatientForm();
        when(patientService.updatePatient(eq(3L), any(PatientForm.class))).thenReturn(Mono.error(PANNE_PATIENTS));

        String vue = controller.updatePatient(3L, form, model).block();

        assertThat(vue).isEqualTo("patients/patient-update");
        assertThat(model.getAttribute("patient")).isEqualTo(form);
        assertThat(model.getAttribute("errorMessage")).isEqualTo(PANNE_PATIENTS.getErrorMessage());
    }
}
