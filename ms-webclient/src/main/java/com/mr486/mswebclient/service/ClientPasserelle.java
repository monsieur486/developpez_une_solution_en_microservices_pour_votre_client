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

@Component
@Slf4j
public class ClientPasserelle {
    private static final String MOTIF_INTROUVABLE_PAR_DEFAUT = "La ressource demandée est introuvable.";

    private static final String MOTIF_DOUBLON_PAR_DEFAUT = "La ressource existe déjà.";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String urlDeLaPasserelle;

    public ClientPasserelle(
            RestTemplate restTemplate,
            ObjectMapper objectMapper,
            @Value("${app.gateway.base-url}") String urlDeLaPasserelle) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.urlDeLaPasserelle = urlDeLaPasserelle;
    }

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
