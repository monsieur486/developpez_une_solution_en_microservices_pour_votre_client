package com.mr486.mswebclient.service;

import com.mr486.mswebclient.dto.ErrorMessage;
import com.mr486.mswebclient.dto.Note;
import com.mr486.mswebclient.dto.PageReponse;
import com.mr486.mswebclient.exception.GatewayException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Accède aux notes médicales exposées par ms-notes au travers de la passerelle,
 * sans bloquer.
 *
 * <p><b>Exemple :</b> getNotesPagines(7L, 0) émet les 5 dernières notes du
 * patient 7 ; createNote(7L, note) en ajoute une nouvelle.</p>
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
     * Retourne une page des notes d'un patient (les 5 dernières, de la plus
     * récente à la plus ancienne), sans bloquer.
     *
     * <p><b>Exemple :</b> getNotesPagines(7L, 0) émet les 5 dernières notes du
     * patient 7 avec les informations de navigation ; une panne de la passerelle
     * propage une {@link GatewayException}.</p>
     *
     * @param patientId identifiant du patient
     * @param page      numéro de la page demandée (à partir de 0)
     * @return un Mono émettant la page de notes
     */
    public Mono<PageReponse<Note>> getNotesPagines(Long patientId, int page) {
        return gatewayWebClient.get()
                .uri("/ms-notes/patients/{id}/notes/pagines?page={page}", patientId, page)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<PageReponse<Note>>() {
                })
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
