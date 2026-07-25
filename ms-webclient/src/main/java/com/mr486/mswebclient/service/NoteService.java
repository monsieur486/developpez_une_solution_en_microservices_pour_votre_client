package com.mr486.mswebclient.service;

import com.mr486.mswebclient.dto.ErrorMessage;
import com.mr486.mswebclient.dto.Note;
import com.mr486.mswebclient.exception.GatewayException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Accède aux notes médicales exposées par ms-notes au travers de la passerelle,
 * sans bloquer.
 *
 * <p><b>Exemple :</b> getNotesByPatientId(7L) émet les notes du patient 7 ;
 * createNote(7L, note) en ajoute une nouvelle.</p>
 */
@Service
public class NoteService {

    /** Nom du microservice cité dans les messages d'erreur de repli. */
    private static final String MICROSERVICE = "ms-notes";

    private final WebClient gatewayWebClient;

    /**
     * Construit le service avec le client REST réactif de la passerelle.
     *
     * <p><b>Exemple :</b> new NoteService(gatewayWebClient) appellera
     * /ms-notes/** au travers de la passerelle.</p>
     *
     * @param gatewayWebClient le client REST authentifié vers la passerelle
     */
    public NoteService(WebClient gatewayWebClient) {
        this.gatewayWebClient = gatewayWebClient;
    }

    /**
     * Retourne les notes médicales d'un patient, sans bloquer.
     *
     * <p><b>Exemple :</b> getNotesByPatientId(7L) émet les notes du patient 7
     * (flux vide si aucune) ; une panne de la passerelle propage une
     * {@link GatewayException}.</p>
     *
     * @param patientId identifiant du patient
     * @return un Flux émettant les notes du patient
     */
    public Flux<Note> getNotesByPatientId(Long patientId) {
        return gatewayWebClient.get()
                .uri("/ms-notes/patients/{id}/notes", patientId)
                .retrieve()
                .bodyToFlux(Note.class)
                .onErrorMap(this::estUneErreurTechnique, e -> repli());
    }

    /**
     * Crée une note médicale pour un patient, sans bloquer.
     *
     * <p><b>Exemple :</b> createNote(7L, note) ajoute la note au dossier du
     * patient 7.</p>
     *
     * @param patientId identifiant du patient concerné
     * @param note      la note à créer
     * @return un Mono complété quand la création est faite
     */
    public Mono<Void> createNote(Long patientId, Note note) {
        return gatewayWebClient.post()
                .uri("/ms-notes/patients/{id}/notes", patientId)
                .bodyValue(note)
                .retrieve()
                .bodyToMono(Void.class)
                .onErrorMap(this::estUneErreurTechnique, e -> repli());
    }

    // Vraie panne (connexion, délai…) : tout sauf une erreur distante déjà traduite.
    private boolean estUneErreurTechnique(Throwable e) {
        return !(e instanceof GatewayException);
    }

    // Message de repli nominatif affiché quand la passerelle est injoignable.
    private GatewayException repli() {
        return new GatewayException(
                new ErrorMessage(HttpStatus.SERVICE_UNAVAILABLE.value(), MICROSERVICE + " ne répond pas."));
    }
}
