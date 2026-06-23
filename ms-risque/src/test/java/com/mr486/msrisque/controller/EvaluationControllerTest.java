package com.mr486.msrisque.controller;

import com.mr486.msrisque.dto.Risque;
import com.mr486.msrisque.service.EvaluationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EvaluationControllerTest {

    @Mock
    private EvaluationService evaluationService;

    @InjectMocks
    private EvaluationController evaluationController;

    @Test
    void evaluate_retourne200EtLeRisque() {
        when(evaluationService.evalueRisque(7L)).thenReturn(new Risque("Borderline"));

        ResponseEntity<Risque> reponse = evaluationController.evaluate(7L);

        assertThat(reponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(reponse.getBody()).isNotNull();
        assertThat(reponse.getBody().getLevel()).isEqualTo("Borderline");
    }
}
