package com.mr486.mswebclient.controller;

import com.mr486.mswebclient.dto.ErrorMessage;
import com.mr486.mswebclient.dto.Risque;
import com.mr486.mswebclient.exception.GatewayException;
import com.mr486.mswebclient.service.EvaluationService;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EvaluationControllerTest {

    private static final GatewayException PANNE =
            new GatewayException(new ErrorMessage(503, "ms-risque ne répond pas."));

    private final EvaluationService evaluationService = mock(EvaluationService.class);
    private final EvaluationController controller = new EvaluationController(evaluationService);

    private final Model model = new ExtendedModelMap();

    @Test
    void getEvaluation_remplitLeModeleAvecLeNiveauDeRisque() {
        when(evaluationService.getEvaluationByPatientId(7L)).thenReturn(Mono.just(new Risque("Borderline")));

        String vue = controller.getEvaluation(model, 7L).block();

        assertThat(vue).isEqualTo("evaluation/evaluation");
        assertThat(model.getAttribute("evaluation")).isEqualTo("Borderline");
        assertThat(model.getAttribute("patientId")).isEqualTo(7L);
    }

    @Test
    void getEvaluation_afficheLErreurQuandLeServiceEchoue() {
        when(evaluationService.getEvaluationByPatientId(7L)).thenReturn(Mono.error(PANNE));

        String vue = controller.getEvaluation(model, 7L).block();

        assertThat(vue).isEqualTo("evaluation/evaluation");
        assertThat(model.getAttribute("errorMessage")).isEqualTo(PANNE.getErrorMessage());
        assertThat(model.getAttribute("patientId")).isEqualTo(7L);
    }
}
