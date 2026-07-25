package com.mr486.msrisque.service;

import com.mr486.msrisque.dto.Patient;
import java.io.IOException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import static org.assertj.core.api.Assertions.assertThat;

class PatientServiceTest {

    private MockWebServer serveur;
    private PatientService patientService;

    @BeforeEach
    void init() throws IOException {
        serveur = new MockWebServer();
        serveur.start();
        WebClient webClient = WebClient.builder().baseUrl(serveur.url("/").toString()).build();
        patientService = new PatientService(webClient);
    }

    @AfterEach
    void arret() throws IOException {
        serveur.shutdown();
    }

    @Test
    void getPatientById_appelleMsPatientsEtRetourneLePatient() throws InterruptedException {
        serveur.enqueue(new MockResponse()
                .setBody("{\"id\":7,\"firstName\":\"Test\",\"lastName\":\"TestInDanger\",\"gender\":\"M\"}")
                .addHeader("Content-Type", "application/json"));

        Patient patient = patientService.getPatientById(7L);

        assertThat(patient.getId()).isEqualTo(7L);
        assertThat(patient.getLastName()).isEqualTo("TestInDanger");
        assertThat(serveur.takeRequest().getPath()).isEqualTo("/patients/7");
    }
}
