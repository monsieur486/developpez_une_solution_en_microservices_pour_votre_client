package com.mr486.mswebclient.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mr486.commun.dto.ErrorResponse;
import com.mr486.commun.exception.AppelRefuseException;
import com.mr486.commun.exception.DuplicateException;
import com.mr486.commun.exception.ResourceNotFoundException;
import com.mr486.commun.exception.ServiceIndisponibleException;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Seul point de contact de l'interface web avec la gateway.
 *
 * <p>Traduit les réponses HTTP en exceptions métier, pour que les contrôleurs ne manipulent
 * jamais d'erreur technique : 404 → {@code ResourceNotFoundException}, 409 →
 * {@code DuplicateException}, autre 4xx → {@code AppelRefuseException}, absence de réponse ou
 * 5xx → {@code ServiceIndisponibleException}.</p>
 *
 * <p><b>Exemple :</b> créer un patient déjà existant lève {@code DuplicateException} avec le motif
 * renvoyé par {@code ms-patients}.</p>
 */
@Component
@Slf4j
public class ClientPasserelle {
    private static final String MOTIF_INTROUVABLE_PAR_DEFAUT = "La ressource demandée est introuvable.";

    private static final String MOTIF_DOUBLON_PAR_DEFAUT = "La ressource existe déjà.";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String urlDeLaPasserelle;

    /**
     * Construit le client à partir de ses collaborateurs.
     *
     * <p><b>Exemple :</b> {@code new ClientPasserelle(restTemplate, objectMapper, "http://ms-gateway:9000")}.</p>
     *
     * @param restTemplate      client HTTP authentifié vers la gateway
     * @param objectMapper      lecteur JSON des corps d'erreur renvoyés par les microservices
     * @param urlDeLaPasserelle adresse de la gateway, sans barre finale
     */
    public ClientPasserelle(
            RestTemplate restTemplate,
            ObjectMapper objectMapper,
            @Value("${app.gateway.base-url}") String urlDeLaPasserelle) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.urlDeLaPasserelle = urlDeLaPasserelle;
    }

    /**
     * Envoie une requête à un microservice via la gateway et renvoie le corps de la réponse.
     *
     * <p><b>Exemple :</b> {@code echange(GET, "/ms-patients/patients/1", null, type, "ms-patients")}
     * renvoie la fiche du patient 1.</p>
     *
     * @param methode méthode HTTP
     * @param chemin  chemin à partir de la gateway, préfixé du nom du microservice
     * @param corps   corps à envoyer en JSON, {@code null} s'il n'y en a pas
     * @param type    type attendu du corps de réponse
     * @param service nom du microservice appelé, repris dans les messages d'erreur
     * @param <T>     type du corps de réponse
     * @return le corps de la réponse convertie
     * @throws com.mr486.commun.exception.ResourceNotFoundException   si le microservice répond 404
     * @throws com.mr486.commun.exception.DuplicateException          si le microservice répond 409
     * @throws com.mr486.commun.exception.AppelRefuseException        sur toute autre réponse 4xx
     * @throws com.mr486.commun.exception.ServiceIndisponibleException si le microservice est injoignable
     */
    public <T> T echange(
            HttpMethod methode,
            String chemin,
            Object corps,
            ParameterizedTypeReference<T> type,
            String service) {
        try {
            HttpEntity<Object> requete = corps == null ? null : new HttpEntity<>(corps);
            return restTemplate.exchange(urlDeLaPasserelle + chemin, methode, requete, type).getBody();
        } catch (HttpClientErrorException ex) {
            throw traduitLeRefus(ex, methode, chemin, service);
        } catch (RestClientException ex) {
            throw new ServiceIndisponibleException(service, ex);
        }
    }

    // Une réponse 4xx vient d'un service qui répond : 404 et 409 sont des cas métier, le reste un refus.
    private RuntimeException traduitLeRefus(
            HttpClientErrorException ex, HttpMethod methode, String chemin, String service) {
        if (ex.getStatusCode().isSameCodeAs(HttpStatus.NOT_FOUND)) {
            log.warn("ressource absente sur {} : {} {}", service, methode, chemin);
            return new ResourceNotFoundException(motifDuMicroservice(ex, MOTIF_INTROUVABLE_PAR_DEFAUT), ex);
        }
        if (ex.getStatusCode().isSameCodeAs(HttpStatus.CONFLICT)) {
            log.warn("conflit sur {} : {} {}", service, methode, chemin);
            return new DuplicateException(motifDuMicroservice(ex, MOTIF_DOUBLON_PAR_DEFAUT), ex);
        }
        log.debug("requête refusée par {} : {} {}", service, methode, chemin);
        return new AppelRefuseException(service, ex.getStatusCode().value(), ex);
    }

    // Reprend le motif renvoyé par le microservice ; un corps absent ou illisible garde le motif par défaut.
    private String motifDuMicroservice(HttpClientErrorException ex, String motifParDefaut) {
        try {
            ErrorResponse erreur = objectMapper.readValue(
                    ex.getResponseBodyAsByteArray(), ErrorResponse.class);
            if (erreur.getMessages() == null || erreur.getMessages().isEmpty()) {
                return motifParDefaut;
            }
            return String.join(" ", erreur.getMessages());
        } catch (IOException illisible) {
            log.debug("corps du {} illisible, motif par défaut conservé",
                    ex.getStatusCode().value(), illisible);
            return motifParDefaut;
        }
    }
}
