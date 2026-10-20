package com.mr486.mswebclient.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.mr486.commun.dto.PatientDto;
import com.mr486.commun.dto.PatientForm;
import com.mr486.commun.exception.DuplicateException;
import com.mr486.mswebclient.configuration.GatewaySecurityConfiguration;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;

// Le RestTemplate est le vrai, construit par Spring Boot comme en production (même Jackson) :
// seule la gateway est simulée. On vérifie ainsi les URL appelées, l'en-tête Basic et le JSON.
@RestClientTest(PatientApiService.class)
@Import({GatewaySecurityConfiguration.class, ClientPasserelle.class})
@TestPropertySource(properties = {
    "app.gateway.base-url=http://localhost:9000",
    "app.auth.username=praticien",
    "app.auth.password=secret"})
@DisplayName("Appels de l'API patients à travers la gateway")
class PatientApiServiceTest {
    private static final String GATEWAY = "http://localhost:9000";

    private static final String PATIENTS = GATEWAY + "/ms-patients/patients";

    private static final String BASIC_ATTENDU = "Basic " + Base64.getEncoder()
            .encodeToString("praticien:secret".getBytes(StandardCharsets.UTF_8));

    private static final String FICHE_TEST_NONE = """
            {"id":1,"firstName":"Test","lastName":"TestNone","birthDate":"1966-12-31","gender":"F",
             "postalAddress":"1 Brookside St","phoneNumber":"100-222-3333"}
            """;

    @Autowired
    private MockRestServiceServer gateway;

    @Autowired
    private PatientApiService patientApiService;

    @Test
    @DisplayName("la liste est demandée en GET authentifié et convertie, date comprise")
    void listeLesPatients_appelleLaGatewayEtConvertitLaReponse() {
        gateway.expect(requestTo(PATIENTS))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, BASIC_ATTENDU))
                .andRespond(withSuccess("[" + FICHE_TEST_NONE + "]", MediaType.APPLICATION_JSON));

        List<PatientDto> patients = patientApiService.listeLesPatients();

        gateway.verify();
        assertThat(patients).singleElement()
                .extracting(PatientDto::getLastName, PatientDto::getBirthDate)
                .containsExactly("TestNone", LocalDate.of(1966, 12, 31));
    }

    @Test
    @DisplayName("la fiche est demandée sur le chemin portant l'identifiant")
    void recupereLePatient_appelleLeCheminDuPatient() {
        gateway.expect(requestTo(PATIENTS + "/1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(FICHE_TEST_NONE, MediaType.APPLICATION_JSON));

        PatientDto patient = patientApiService.recupereLePatient(1L);

        gateway.verify();
        assertThat(patient.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("la création envoie le formulaire en JSON par POST, date au format ISO")
    void creeLePatient_envoieLeFormulaireEnJson() {
        gateway.expect(requestTo(PATIENTS))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, BASIC_ATTENDU))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.lastName").value("TestNone"))
                .andExpect(jsonPath("$.birthDate").value("1966-12-31"))
                .andRespond(withStatus(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON).body(FICHE_TEST_NONE));

        patientApiService.creeLePatient(unFormulaire());

        gateway.verify();
    }

    @Test
    @DisplayName("la mise à jour envoie le formulaire par PUT sur le chemin du patient")
    void metAJourLePatient_envoieUnPutSurLeCheminDuPatient() {
        gateway.expect(requestTo(PATIENTS + "/1"))
                .andExpect(method(HttpMethod.PUT))
                .andExpect(jsonPath("$.firstName").value("Test"))
                .andRespond(withSuccess(FICHE_TEST_NONE, MediaType.APPLICATION_JSON));

        patientApiService.metAJourLePatient(1L, unFormulaire());

        gateway.verify();
    }

    // De bout en bout : c'est le vrai RestTemplate qui lève l'erreur HTTP, pas un mock.
    @Test
    @DisplayName("un 409 de la gateway devient un doublon portant le motif du microservice")
    void creeLePatient_reponse409_leveUnDoublonAvecLeMotif() {
        gateway.expect(requestTo(PATIENTS))
                .andRespond(withStatus(HttpStatus.CONFLICT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("""
                                {"errorCode":"CONFLICT","messages":["Le patient existe déjà dans la base de données."]}
                                """));

        assertThatThrownBy(() -> patientApiService.creeLePatient(unFormulaire()))
                .isInstanceOf(DuplicateException.class)
                .hasMessage("Le patient existe déjà dans la base de données.");
    }

    // Formulaire valide reprenant le patient de démonstration TestNone.
    private PatientForm unFormulaire() {
        return PatientForm.builder()
                .firstName("Test")
                .lastName("TestNone")
                .birthDate(LocalDate.of(1966, 12, 31))
                .gender("F")
                .build();
    }
}
