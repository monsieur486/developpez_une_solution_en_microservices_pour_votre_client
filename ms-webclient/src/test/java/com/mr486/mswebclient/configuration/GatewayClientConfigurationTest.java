package com.mr486.mswebclient.configuration;

import com.mr486.mswebclient.dto.Patient;
import com.mr486.mswebclient.exception.GatewayException;
import java.io.IOException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GatewayClientConfigurationTest {

    private final GatewayClientConfiguration configuration = new GatewayClientConfiguration();
    private MockWebServer serveur;
    private WebClient client;

    @BeforeEach
    void init() throws IOException {
        serveur = new MockWebServer();
        serveur.start();
        ReflectionTestUtils.setField(configuration, "gatewayBase", serveur.url("/").toString());
        ReflectionTestUtils.setField(configuration, "username", "user");
        ReflectionTestUtils.setField(configuration, "password", "pass");
        client = configuration.gatewayWebClient(WebClient.builder());
    }

    @AfterEach
    void arret() throws IOException {
        serveur.shutdown();
    }

    @Test
    void gatewayWebClient_envoieLAuthentificationBasic() throws InterruptedException {
        serveur.enqueue(new MockResponse().setBody("{\"id\":7}")
                .addHeader("Content-Type", "application/json"));

        Patient patient = client.get().uri("/ms-patients/patients/7").retrieve()
                .bodyToMono(Patient.class).block();

        assertThat(patient).isNotNull();
        assertThat(serveur.takeRequest().getHeader("Authorization")).startsWith("Basic ");
    }

    @Test
    void gatewayWebClient_decodeLeCorpsDErreurDistant() {
        serveur.enqueue(new MockResponse().setResponseCode(404)
                .setBody("{\"status\":404,\"message\":\"Patient introuvable\"}")
                .addHeader("Content-Type", "application/json"));

        assertThatThrownBy(() -> client.get().uri("/ms-patients/patients/99").retrieve()
                .bodyToMono(Patient.class).block())
                .isInstanceOf(GatewayException.class)
                .hasMessage("Patient introuvable")
                .extracting(e -> ((GatewayException) e).getErrorMessage().getStatus()).isEqualTo(404);
    }

    @Test
    void gatewayWebClient_replieSurUnMessageParDefautQuandCorpsIllisible() {
        serveur.enqueue(new MockResponse().setResponseCode(503).setBody("pas du json"));

        assertThatThrownBy(() -> client.get().uri("/ms-patients/patients").retrieve()
                .bodyToMono(Patient.class).block())
                .isInstanceOf(GatewayException.class)
                .hasMessageContaining("Le service ne répond pas");
    }

    @Test
    void gatewayWebClient_replieSurUnMessageParDefautQuandCorpsVide() {
        serveur.enqueue(new MockResponse().setResponseCode(500));

        assertThatThrownBy(() -> client.get().uri("/ms-patients/patients").retrieve()
                .bodyToMono(Patient.class).block())
                .isInstanceOf(GatewayException.class)
                .hasMessageContaining("Le service ne répond pas");
    }
}
