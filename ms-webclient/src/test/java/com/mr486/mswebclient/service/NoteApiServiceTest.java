package com.mr486.mswebclient.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.mr486.commun.dto.NoteDto;
import com.mr486.commun.dto.NoteForm;
import com.mr486.commun.exception.ServiceIndisponibleException;
import com.mr486.mswebclient.configuration.GatewaySecurityConfiguration;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
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
@RestClientTest(NoteApiService.class)
@Import({GatewaySecurityConfiguration.class, ClientPasserelle.class})
@TestPropertySource(properties = {
    "app.gateway.base-url=http://localhost:9000",
    "app.auth.username=praticien",
    "app.auth.password=secret"})
@DisplayName("Appels de l'API notes à travers la gateway")
class NoteApiServiceTest {
    private static final String NOTES_DU_PATIENT_1 = "http://localhost:9000/ms-notes/patients/1/notes";

    private static final String BASIC_ATTENDU = "Basic " + Base64.getEncoder()
            .encodeToString("praticien:secret".getBytes(StandardCharsets.UTF_8));

    @Autowired
    private MockRestServiceServer gateway;

    @Autowired
    private NoteApiService noteApiService;

    @Test
    @DisplayName("les notes sont demandées en GET authentifié sur le chemin du patient")
    void listeLesNotes_appelleLaGatewayEtConvertitLaReponse() {
        gateway.expect(requestTo(NOTES_DU_PATIENT_1))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, BASIC_ATTENDU))
                .andRespond(withSuccess("[{\"content\":\"Le patient déclare se sentir bien\","
                                + "\"createdDate\":\"2026-10-20T16:13:39Z\"}]",
                        MediaType.APPLICATION_JSON));

        List<NoteDto> notes = noteApiService.listeLesNotes(1L);

        gateway.verify();
        assertThat(notes).singleElement()
                .returns("Le patient déclare se sentir bien", NoteDto::getContent)
                .returns(Instant.parse("2026-10-20T16:13:39Z"), NoteDto::getCreatedDate);
    }

    @Test
    @DisplayName("l'ajout envoie la note en JSON par POST sur le chemin du patient")
    void ajouteUneNote_envoieLaNoteEnJson() {
        gateway.expect(requestTo(NOTES_DU_PATIENT_1))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, BASIC_ATTENDU))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").value("Poids en hausse"))
                .andRespond(withStatus(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON).body("{\"content\":\"Poids en hausse\"}"));

        NoteDto creee = noteApiService.ajouteUneNote(1L, NoteForm.builder().content("Poids en hausse").build());

        gateway.verify();
        assertThat(creee.getContent()).isEqualTo("Poids en hausse");
    }

    @Test
    @DisplayName("une panne de ms-notes devient une indisponibilité nommant le service des notes")
    void listeLesNotes_reponse5xx_leveServiceIndisponible() {
        gateway.expect(requestTo(NOTES_DU_PATIENT_1)).andRespond(withServerError());

        assertThatThrownBy(() -> noteApiService.listeLesNotes(1L))
                .isInstanceOf(ServiceIndisponibleException.class)
                .hasMessage("Le service ms-notes est momentanément indisponible.");
    }
}
