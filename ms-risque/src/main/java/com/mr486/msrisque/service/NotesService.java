package com.mr486.msrisque.service;

import com.mr486.msrisque.dto.Note;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

/**
 * Récupère les notes médicales d'un patient auprès du microservice ms-notes.
 *
 * <p><b>Exemple :</b> getNotesByPatientId(7L) retourne un Flux émettant les
 * notes du patient d'identifiant 7.</p>
 */
@Service
public class NotesService {

    private final WebClient notesWebClient;

    /**
     * Construit le service avec le client WebClient de ms-notes.
     *
     * <p><b>Exemple :</b> new NotesService(notesWebClient) appellera ms-notes au
     * travers du client configuré.</p>
     *
     * @param notesWebClient le client WebClient configuré pour ms-notes
     */
    public NotesService(@Qualifier("notesWebClient") WebClient notesWebClient) {
        this.notesWebClient = notesWebClient;
    }

    /**
     * Récupère les notes médicales d'un patient, sans bloquer.
     *
     * <p><b>Exemple :</b> getNotesByPatientId(7L) appelle GET /patients/7/notes
     * et émet les notes du patient 7 (flux vide si aucune) ; une erreur distante
     * propage {@link com.mr486.msrisque.exception.RemoteServiceException}.</p>
     *
     * @param patientId identifiant du patient
     * @return un Flux émettant les notes du patient
     */
    public Flux<Note> getNotesByPatientId(Long patientId) {
        return notesWebClient.get()
                .uri("/patients/{id}/notes", patientId)
                .retrieve()
                .bodyToFlux(Note.class);
    }
}
