package com.mr486.mswebclient.service;

import com.mr486.mswebclient.dto.Risque;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EvaluationServiceTest {

    private static final String BASE = "http://gateway";

    private final RestTemplate restTemplate = mock(RestTemplate.class);
    private final EvaluationService service = new EvaluationService(restTemplate, BASE);

    @Test
    void getEvaluationByPatientId_appelleLaPasserelleEtRetourneLeRisque() {
        Risque attendu = new Risque("Borderline");
        when(restTemplate.exchange(eq(BASE + "/ms-risque/patients/7/evaluation"), eq(HttpMethod.GET),
                isNull(), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(attendu));

        Risque risque = service.getEvaluationByPatientId(7L);

        assertThat(risque.getLevel()).isEqualTo("Borderline");
    }
}
