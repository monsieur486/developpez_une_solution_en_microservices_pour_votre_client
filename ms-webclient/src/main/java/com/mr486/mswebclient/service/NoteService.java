package com.mr486.mswebclient.service;

import com.mr486.mswebclient.dto.Note;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * Accède aux notes médicales exposées par ms-notes au travers de la passerelle.
 *
 * <p><b>Exemple :</b> getNotesByPatientId(7L) retourne les notes du patient 7 ;
 * createNote(7L, note) en ajoute une nouvelle.</p>
 */
@Service
public class NoteService {

    private final RestTemplate restTemplate;
    private final String gatewayBase;

    /**
     * Construit le service avec le client REST et l'URL de la passerelle.
     *
     * <p><b>Exemple :</b> new NoteService(restTemplate, "http://localhost:9000")
     * appellera http://localhost:9000/ms-notes/patients/7/notes.</p>
     *
     * @param restTemplate le client REST authentifié vers la passerelle
     * @param gatewayBase  l'URL de base de la passerelle
     */
    public NoteService(RestTemplate restTemplate,
                       @Value("${app.gateway.base-url}") String gatewayBase) {
        this.restTemplate = restTemplate;
        this.gatewayBase = gatewayBase;
    }

    /**
     * Retourne les notes médicales d'un patient.
     *
     * <p><b>Exemple :</b> getNotesByPatientId(7L) retourne les notes du patient 7
     * (liste vide si aucune).</p>
     *
     * @param patientId identifiant du patient
     * @return la liste des notes du patient
     */
    public List<Note> getNotesByPatientId(Long patientId) {
        ResponseEntity<List<Note>> response = restTemplate.exchange(
                gatewayBase + "/ms-notes/patients/" + patientId + "/notes",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );
        return response.getBody();
    }

    /**
     * Crée une note médicale pour un patient.
     *
     * <p><b>Exemple :</b> createNote(7L, note) ajoute la note au dossier du
     * patient 7.</p>
     *
     * @param patientId identifiant du patient concerné
     * @param note      la note à créer
     */
    public void createNote(Long patientId, Note note) {
        restTemplate.exchange(
                gatewayBase + "/ms-notes/patients/" + patientId + "/notes",
                HttpMethod.POST,
                new HttpEntity<>(note),
                new ParameterizedTypeReference<>() {
                }
        );
    }
}
