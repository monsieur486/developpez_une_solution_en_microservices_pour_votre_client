package com.mr486.commun.exception;

import com.mr486.commun.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Journalise une erreur et fabrique la réponse normalisée {@link ErrorResponse} correspondante.
 *
 * <p>Séparée de {@link GlobalExceptionHandler} : celui-ci décide <i>quel</i> statut et quel
 * message chaque exception mérite ; cette classe sait <i>comment</i> le journaliser et
 * l'envoyer. Ajouter une exception à traduire ne touche donc qu'au gestionnaire.</p>
 *
 * <p><b>Exemple :</b> {@code client(HttpStatus.NOT_FOUND, List.of("Aucun patient avec l'id: 42"), requete)}
 * journalise un avertissement et renvoie un 404 signé du nom du microservice.</p>
 */
@Slf4j
final class ReponsesErreur {

    /** Nom du microservice, repris dans chaque réponse pour situer l'erreur. */
    private final String nomDuMicroservice;

    /**
     * Construit la fabrique pour un microservice donné.
     *
     * <p><b>Exemple :</b> {@code new ReponsesErreur("ms-patients")}.</p>
     *
     * @param nomDuMicroservice nom repris dans le champ {@code microserviceName} des réponses
     */
    ReponsesErreur(String nomDuMicroservice) {
        this.nomDuMicroservice = nomDuMicroservice;
    }

    /**
     * Répond à une erreur imputable à l'appelant : le message lui est utile, l'incident reste bénin.
     *
     * <p><b>Exemple :</b> un formulaire invalide est journalisé en {@code warn} et renvoyé en 400
     * avec ses messages de validation.</p>
     *
     * @param statut   statut 4xx à renvoyer
     * @param messages messages explicatifs destinés à l'appelant
     * @param requete  requête à l'origine de l'erreur
     * @return la réponse normalisée
     */
    ResponseEntity<Object> client(HttpStatus statut, List<String> messages, HttpServletRequest requete) {
        log.warn("requête refusée ({}) sur {} : {}", statut.value(), requete.getRequestURI(), messages);
        return reponse(statut, messages, requete);
    }

    /**
     * Répond à une erreur imputable au service : l'appelant n'obtient que le message prévu,
     * la cause complète est tracée.
     *
     * <p><b>Exemple :</b> une panne de base est journalisée en {@code error} avec sa pile d'appels
     * et renvoyée en 500 avec « Une erreur interne est survenue. ».</p>
     *
     * @param statut   statut 5xx à renvoyer
     * @param messages messages sans détail technique, destinés à l'appelant
     * @param requete  requête à l'origine de l'erreur
     * @param ex       cause, journalisée avec sa pile d'appels
     * @return la réponse normalisée
     */
    ResponseEntity<Object> serveur(
            HttpStatus statut, List<String> messages, HttpServletRequest requete, Exception ex) {
        log.error("échec du traitement ({}) sur {}", statut.value(), requete.getRequestURI(), ex);
        return reponse(statut, messages, requete);
    }

    // Assemble le corps normalisé renvoyé au client.
    private ResponseEntity<Object> reponse(
            HttpStatus statut, List<String> messages, HttpServletRequest requete) {
        ErrorResponse corps = ErrorResponse.builder()
                .timestamp(Instant.now().toString())
                .path(requete.getRequestURI())
                .errorCode(statut.name())
                .microserviceName(nomDuMicroservice)
                .messages(messages)
                .build();
        return ResponseEntity.status(statut).body(corps);
    }
}
