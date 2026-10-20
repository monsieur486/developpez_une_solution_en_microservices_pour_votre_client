package com.mr486.mswebclient.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mr486.commun.exception.AppelRefuseException;
import com.mr486.commun.exception.DuplicateException;
import com.mr486.commun.exception.ResourceNotFoundException;
import com.mr486.commun.exception.ServiceIndisponibleException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

// Les imports viennent des outils de test (JUnit, Mockito, AssertJ, doublures HTTP), pas d'un couplage métier.
@SuppressWarnings("PMD.ExcessiveImports")
@ExtendWith(MockitoExtension.class)
@DisplayName("Appels aux microservices via la passerelle")
class ClientPasserelleTest {
    private static final String URL_PASSERELLE = "http://localhost:9000";

    private static final String CHEMIN = "/ms-patients/patients";

    private static final String SERVICE = "ms-patients";

    private static final ParameterizedTypeReference<String> TYPE_ATTENDU =
            new ParameterizedTypeReference<>() {};

    @Mock
    private RestTemplate restTemplate;

    private ClientPasserelle clientPasserelle;

    @BeforeEach
    void initialiseLeClient() {
        clientPasserelle = new ClientPasserelle(restTemplate, new ObjectMapper(), URL_PASSERELLE);
    }

    @Test
    @DisplayName("un appel abouti retourne le corps de la réponse")
    void appelAbouti_retourneLeCorps() {
        when(restTemplate.exchange(
                eq(URL_PASSERELLE + CHEMIN), eq(HttpMethod.GET),
                any(), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok("contenu"));

        String resultat = clientPasserelle.echange(
                HttpMethod.GET, CHEMIN, null, TYPE_ATTENDU, SERVICE);

        assertThat(resultat).isEqualTo("contenu");
    }

    @Test
    @DisplayName("le corps transmis est encapsulé dans la requête envoyée")
    void appelAvecCorps_transmetLeCorps() {
        when(restTemplate.exchange(
                anyString(), eq(HttpMethod.POST),
                any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok("créé"));

        clientPasserelle.echange(HttpMethod.POST, CHEMIN, "charge utile", TYPE_ATTENDU, SERVICE);

        ArgumentCaptor<HttpEntity<Object>> requeteEnvoyee = ArgumentCaptor.captor();
        verify(restTemplate).exchange(
                anyString(), eq(HttpMethod.POST),
                requeteEnvoyee.capture(), any(ParameterizedTypeReference.class));
        assertThat(requeteEnvoyee.getValue().getBody()).isEqualTo("charge utile");
    }

    @Test
    @DisplayName("une réponse 404 devient une ressource introuvable, pas une panne")
    void reponse404_leveResourceNotFound() {
        when(restTemplate.exchange(
                anyString(), any(HttpMethod.class), any(), any(ParameterizedTypeReference.class)))
                .thenThrow(HttpClientErrorException.create(
                        HttpStatus.NOT_FOUND, "Not Found", HttpHeaders.EMPTY,
                        new byte[0], StandardCharsets.UTF_8));

        assertThatThrownBy(() -> clientPasserelle.echange(
                HttpMethod.GET, CHEMIN, null, TYPE_ATTENDU, SERVICE))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("La ressource demandée est introuvable.");
    }

    @Test
    @DisplayName("une réponse 404 transmet le motif renvoyé par le microservice")
    void reponse404_transmetLeMotifDuMicroservice() {
        String corps = """
                {"errorCode":"NOT_FOUND","messages":["Aucun patient avec l'id: 42"]}
                """;
        when(restTemplate.exchange(
                anyString(), any(HttpMethod.class), any(), any(ParameterizedTypeReference.class)))
                .thenThrow(HttpClientErrorException.create(
                        HttpStatus.NOT_FOUND, "Not Found", HttpHeaders.EMPTY,
                        corps.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));

        assertThatThrownBy(() -> clientPasserelle.echange(
                HttpMethod.GET, CHEMIN + "/42", null, TYPE_ATTENDU, SERVICE))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Aucun patient avec l'id: 42");
    }

    @Test
    @DisplayName("un service injoignable devient une indisponibilité nommée")
    void serviceInjoignable_leveServiceIndisponible() {
        when(restTemplate.exchange(
                anyString(), any(HttpMethod.class), any(), any(ParameterizedTypeReference.class)))
                .thenThrow(new ResourceAccessException("connexion refusée"));

        assertThatThrownBy(() -> clientPasserelle.echange(
                HttpMethod.GET, CHEMIN, null, TYPE_ATTENDU, SERVICE))
                .isInstanceOf(ServiceIndisponibleException.class)
                .hasMessageContaining(SERVICE);
    }

    @ParameterizedTest(name = "statut {0}")
    @ValueSource(ints = {400, 401, 403})
    @DisplayName("une autre réponse 4xx devient un appel refusé, pas une panne")
    void autreReponse4xx_leveAppelRefuse(int statut) {
        when(restTemplate.exchange(
                anyString(), any(HttpMethod.class), any(), any(ParameterizedTypeReference.class)))
                .thenThrow(HttpClientErrorException.create(
                        HttpStatus.valueOf(statut), "refusé", HttpHeaders.EMPTY,
                        new byte[0], StandardCharsets.UTF_8));

        assertThatThrownBy(() -> clientPasserelle.echange(
                HttpMethod.GET, CHEMIN, null, TYPE_ATTENDU, SERVICE))
                .isInstanceOf(AppelRefuseException.class)
                .hasMessage("Le service ms-patients a refusé la requête (statut " + statut + ").");
    }

    @Test
    @DisplayName("une réponse 409 devient un doublon, pas une panne")
    void reponse409_leveDuplicate() {
        when(restTemplate.exchange(
                anyString(), any(HttpMethod.class), any(), any(ParameterizedTypeReference.class)))
                .thenThrow(HttpClientErrorException.create(
                        HttpStatus.CONFLICT, "Conflict", HttpHeaders.EMPTY,
                        new byte[0], StandardCharsets.UTF_8));

        assertThatThrownBy(() -> clientPasserelle.echange(
                HttpMethod.POST, CHEMIN, "charge utile", TYPE_ATTENDU, SERVICE))
                .isInstanceOf(DuplicateException.class);
    }

    @Test
    @DisplayName("une réponse 409 transmet le motif renvoyé par le microservice")
    void reponse409_transmetLeMotifDuMicroservice() {
        String corps = """
                {"errorCode":"CONFLICT","messages":["Le patient Jean Dupont existe déjà."]}
                """;
        when(restTemplate.exchange(
                anyString(), any(HttpMethod.class), any(), any(ParameterizedTypeReference.class)))
                .thenThrow(HttpClientErrorException.create(
                        HttpStatus.CONFLICT, "Conflict", HttpHeaders.EMPTY,
                        corps.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));

        assertThatThrownBy(() -> clientPasserelle.echange(
                HttpMethod.POST, CHEMIN, "charge utile", TYPE_ATTENDU, SERVICE))
                .isInstanceOf(DuplicateException.class)
                .hasMessage("Le patient Jean Dupont existe déjà.");
    }

    @Test
    @DisplayName("une réponse 409 au corps illisible garde un motif générique")
    void reponse409CorpsIllisible_gardeUnMotifGenerique() {
        when(restTemplate.exchange(
                anyString(), any(HttpMethod.class), any(), any(ParameterizedTypeReference.class)))
                .thenThrow(HttpClientErrorException.create(
                        HttpStatus.CONFLICT, "Conflict", HttpHeaders.EMPTY,
                        "<html>pas du JSON</html>".getBytes(StandardCharsets.UTF_8),
                        StandardCharsets.UTF_8));

        assertThatThrownBy(() -> clientPasserelle.echange(
                HttpMethod.POST, CHEMIN, "charge utile", TYPE_ATTENDU, SERVICE))
                .isInstanceOf(DuplicateException.class)
                .hasMessage("La ressource existe déjà.");
    }
}
