package com.mr486.mswebclient.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.mr486.commun.exception.ServiceIndisponibleException;
import com.mr486.mswebclient.configuration.GatewaySecurityConfiguration;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;

// Le RestTemplate est le vrai, construit par Spring Boot comme en production : seule la gateway
// est simulée. On vérifie ainsi l'URL appelée, l'en-tête Basic et la lecture du niveau en texte brut.
@RestClientTest(EvaluationApiService.class)
@Import({GatewaySecurityConfiguration.class, ClientPasserelle.class})
@TestPropertySource(properties = {
    "app.gateway.base-url=http://localhost:9000",
    "app.auth.username=praticien",
    "app.auth.password=secret"})
@DisplayName("Appel de l'évaluation du risque à travers la gateway")
class EvaluationApiServiceTest {
    private static final String EVALUATION_DU_PATIENT_2 = "http://localhost:9000/ms-risque/evaluation/2";

    private static final String BASIC_ATTENDU = "Basic " + Base64.getEncoder()
            .encodeToString("praticien:secret".getBytes(StandardCharsets.UTF_8));

    @Autowired
    private MockRestServiceServer gateway;

    @Autowired
    private EvaluationApiService evaluationApiService;

    @Test
    @DisplayName("le niveau de risque est demandé en GET authentifié et lu en texte brut")
    void evalueLeRisque_appelleLaGatewayEtRetourneLeNiveau() {
        gateway.expect(requestTo(EVALUATION_DU_PATIENT_2))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, BASIC_ATTENDU))
                .andRespond(withSuccess("Borderline", MediaType.TEXT_PLAIN));

        String niveau = evaluationApiService.evalueLeRisque(2L);

        gateway.verify();
        assertThat(niveau).isEqualTo("Borderline");
    }

    @Test
    @DisplayName("une panne de ms-risque devient une indisponibilité nommant le service du risque")
    void evalueLeRisque_reponse5xx_leveServiceIndisponible() {
        gateway.expect(requestTo(EVALUATION_DU_PATIENT_2)).andRespond(withServerError());

        assertThatThrownBy(() -> evaluationApiService.evalueLeRisque(2L))
                .isInstanceOf(ServiceIndisponibleException.class)
                .hasMessage("Le service ms-risque est momentanément indisponible.");
    }
}
