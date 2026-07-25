package com.mr486.mswebclient.service;

import com.mr486.mswebclient.dto.Risque;
import com.mr486.mswebclient.exception.GatewayException;
import java.io.IOException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EvaluationServiceTest {

    private MockWebServer serveur;
    private EvaluationService service;

    @BeforeEach
    void init() throws IOException {
        serveur = new MockWebServer();
        serveur.start();
        service = new EvaluationService(WebClient.builder().baseUrl(serveur.url("/").toString()).build());
    }

    @AfterEach
    void arret() throws IOException {
        serveur.shutdown();
    }

    @Test
    void getEvaluationByPatientId_appelleLaPasserelleEtEmetLeRisque() throws InterruptedException {
        serveur.enqueue(new MockResponse().setBody("{\"level\":\"Borderline\"}")
                .addHeader("Content-Type", "application/json"));

        Risque risque = service.getEvaluationByPatientId(2L).block();

        assertThat(risque).isNotNull();
        assertThat(risque.getLevel()).isEqualTo("Borderline");
        assertThat(serveur.takeRequest().getPath()).isEqualTo("/ms-risque/patients/2/evaluation");
    }

    @Test
    void getEvaluationByPatientId_replieSurUnMessageNominatifQuandLaPasserelleEstInjoignable() throws IOException {
        serveur.shutdown();

        assertThatThrownBy(() -> service.getEvaluationByPatientId(2L).block())
                .isInstanceOf(GatewayException.class)
                .hasMessage("ms-risque ne répond pas.");
    }
}
