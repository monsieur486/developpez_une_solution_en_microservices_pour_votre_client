package com.mr486.msrisque.configuration;

import com.mr486.msrisque.dto.Patient;
import com.mr486.msrisque.exception.RemoteServiceException;
import java.io.IOException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.loadbalancer.reactive.ReactorLoadBalancerExchangeFilterFunction;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WebClientConfigurationTest {

    private final WebClientConfiguration configuration = new WebClientConfiguration();
    private MockWebServer serveur;

    @BeforeEach
    void init() throws IOException {
        serveur = new MockWebServer();
        serveur.start();
        ReflectionTestUtils.setField(configuration, "user", "user");
        ReflectionTestUtils.setField(configuration, "password", "pass");
        ReflectionTestUtils.setField(configuration, "urlPatients", serveur.url("/").toString());
        ReflectionTestUtils.setField(configuration, "loadBalanced", false);
    }

    @AfterEach
    void arret() throws IOException {
        serveur.shutdown();
    }

    // Construit le client patients pointant vers le serveur HTTP factice.
    private WebClient client() {
        return configuration.patientsWebClient(WebClient.builder(), mock(ObjectProvider.class));
    }

    @Test
    void patientsWebClient_envoieLAuthentificationBasic() throws InterruptedException {
        serveur.enqueue(new MockResponse().setBody("{\"id\":7}")
                .addHeader("Content-Type", "application/json"));

        Patient patient = client().get().uri("/patients/7").retrieve().bodyToMono(Patient.class).block();

        assertThat(patient).isNotNull();
        assertThat(patient.getId()).isEqualTo(7L);
        assertThat(serveur.takeRequest().getHeader("Authorization")).startsWith("Basic ");
    }

    @Test
    void patientsWebClient_decodeLeCorpsDErreurDistant() {
        serveur.enqueue(new MockResponse().setResponseCode(404)
                .setBody("{\"status\":404,\"message\":\"Patient introuvable\"}")
                .addHeader("Content-Type", "application/json"));

        assertThatThrownBy(() -> client().get().uri("/patients/99").retrieve()
                .bodyToMono(Patient.class).block())
                .isInstanceOf(RemoteServiceException.class)
                .hasMessage("Patient introuvable")
                .extracting("httpStatus").isEqualTo(404);
    }

    @Test
    void patientsWebClient_replieSurUnMessageParDefautQuandCorpsIllisible() {
        serveur.enqueue(new MockResponse().setResponseCode(503).setBody("pas du json"));

        assertThatThrownBy(() -> client().get().uri("/patients/7").retrieve()
                .bodyToMono(Patient.class).block())
                .isInstanceOf(RemoteServiceException.class)
                .hasMessageContaining("Le service ne répond pas")
                .extracting("httpStatus").isEqualTo(503);
    }

    @Test
    void patientsWebClient_replieSurUnMessageParDefautQuandCorpsVide() {
        serveur.enqueue(new MockResponse().setResponseCode(500));

        assertThatThrownBy(() -> client().get().uri("/patients/7").retrieve()
                .bodyToMono(Patient.class).block())
                .isInstanceOf(RemoteServiceException.class)
                .hasMessageContaining("Le service ne répond pas");
    }

    @Test
    void notesWebClient_utiliseLeFiltreDeRepartitionQuandActive() {
        ReflectionTestUtils.setField(configuration, "loadBalanced", true);
        ReflectionTestUtils.setField(configuration, "urlNotes", "http://ms-notes");
        ObjectProvider<ReactorLoadBalancerExchangeFilterFunction> provider = mock(ObjectProvider.class);
        when(provider.getObject()).thenReturn(mock(ReactorLoadBalancerExchangeFilterFunction.class));

        configuration.notesWebClient(WebClient.builder(), provider);

        verify(provider).getObject();
    }
}
