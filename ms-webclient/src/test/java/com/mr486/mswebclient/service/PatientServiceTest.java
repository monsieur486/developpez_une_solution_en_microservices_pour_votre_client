package com.mr486.mswebclient.service;

import com.mr486.mswebclient.dto.PageReponse;
import com.mr486.mswebclient.dto.Patient;
import com.mr486.mswebclient.dto.PatientForm;
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

class PatientServiceTest {

    private MockWebServer serveur;
    private PatientService service;

    @BeforeEach
    void init() throws IOException {
        serveur = new MockWebServer();
        serveur.start();
        service = new PatientService(WebClient.builder().baseUrl(serveur.url("/").toString()).build());
    }

    @AfterEach
    void arret() throws IOException {
        serveur.shutdown();
    }

    @Test
    void getPatients_appelleLaPasserelleEtEmetLaPage() throws InterruptedException {
        serveur.enqueue(new MockResponse()
                .setBody("{\"content\":[{\"id\":1},{\"id\":2}],\"page\":0,\"totalPages\":3,\"totalElements\":45}")
                .addHeader("Content-Type", "application/json"));

        PageReponse<Patient> page = service.getPatients(0).block();

        assertThat(page).isNotNull();
        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getTotalPages()).isEqualTo(3);
        assertThat(page.aPlusieursPages()).isTrue();
        assertThat(serveur.takeRequest().getPath()).isEqualTo("/ms-patients/patients?page=0");
    }

    @Test
    void getPatientById_appelleLaPasserelleEtEmetLePatient() throws InterruptedException {
        serveur.enqueue(new MockResponse().setBody("{\"id\":7}")
                .addHeader("Content-Type", "application/json"));

        Patient patient = service.getPatientById(7L).block();

        assertThat(patient).isNotNull();
        assertThat(patient.getId()).isEqualTo(7L);
        assertThat(serveur.takeRequest().getPath()).isEqualTo("/ms-patients/patients/7");
    }

    @Test
    void createPatient_envoieLeFormulaireEnPost() throws InterruptedException {
        serveur.enqueue(new MockResponse().setResponseCode(200));

        service.createPatient(new PatientForm()).block();

        assertThat(serveur.takeRequest().getMethod()).isEqualTo("POST");
    }

    @Test
    void updatePatient_envoieLeFormulaireEnPut() throws InterruptedException {
        serveur.enqueue(new MockResponse().setResponseCode(200));

        service.updatePatient(7L, new PatientForm()).block();

        assertThat(serveur.takeRequest().getMethod()).isEqualTo("PUT");
    }

    @Test
    void getPatients_replieSurUnMessageNominatifQuandLaPasserelleEstInjoignable() throws IOException {
        serveur.shutdown();

        assertThatThrownBy(() -> service.getPatients(0).block())
                .isInstanceOf(GatewayException.class)
                .hasMessage("ms-patients ne répond pas.");
    }
}
