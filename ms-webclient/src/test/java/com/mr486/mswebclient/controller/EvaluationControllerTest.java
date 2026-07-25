package com.mr486.mswebclient.controller;

import com.mr486.mswebclient.dto.ErrorMessage;
import com.mr486.mswebclient.dto.Risque;
import com.mr486.mswebclient.service.EvaluationService;
import com.mr486.mswebclient.tools.ErrorResponseTools;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EvaluationControllerTest {

    private final EvaluationService evaluationService = mock(EvaluationService.class);
    private final ErrorResponseTools errorResponseTools = mock(ErrorResponseTools.class);
    private final EvaluationController controller =
            new EvaluationController(evaluationService, errorResponseTools);

    private final Model model = new ExtendedModelMap();

    @Test
    void getEvaluation_remplitLeModeleAvecLeNiveauDeRisque() {
        when(evaluationService.getEvaluationByPatientId(7L)).thenReturn(new Risque("Borderline"));

        String vue = controller.getEvaluation(model, 7L);

        assertThat(vue).isEqualTo("evaluation/evaluation");
        assertThat(model.getAttribute("evaluation")).isEqualTo("Borderline");
        assertThat(model.getAttribute("patientId")).isEqualTo(7L);
    }

    @Test
    void getEvaluation_afficheLErreurQuandLeServiceEchoue() {
        when(evaluationService.getEvaluationByPatientId(7L)).thenThrow(new RuntimeException("boom"));
        ErrorMessage erreur = new ErrorMessage(503, "ms-risque ne répond pas.");
        when(errorResponseTools.getErrorMessage(anyString(), anyString())).thenReturn(erreur);

        String vue = controller.getEvaluation(model, 7L);

        assertThat(vue).isEqualTo("evaluation/evaluation");
        assertThat(model.getAttribute("errorMessage")).isEqualTo(erreur);
        assertThat(model.getAttribute("patientId")).isEqualTo(7L);
    }
}
