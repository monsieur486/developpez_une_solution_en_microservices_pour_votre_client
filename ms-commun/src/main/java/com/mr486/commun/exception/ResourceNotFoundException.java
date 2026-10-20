package com.mr486.commun.exception;

/**
 * Signale qu'une ressource demandée n'existe pas.
 *
 * <p>Levée par la couche service lorsqu'un identifiant ne correspond à aucun
 * enregistrement ; les gestionnaires d'exception la traduisent en réponse HTTP 404.
 * Le message transporté est destiné à l'appelant et ne doit contenir aucune donnée
 * confidentielle.</p>
 *
 * <p><b>Exemple :</b> {@code throw new ResourceNotFoundException("Aucune ressource avec
 * l'id: 42")} lorsqu'aucun enregistrement ne porte l'identifiant 42.</p>
 */
public class ResourceNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Construit l'exception avec le message présenté à l'appelant.
     *
     * <p><b>Exemple :</b> {@code new ResourceNotFoundException("Aucune ressource avec
     * l'id: 7")}.</p>
     *
     * @param message description de la ressource introuvable
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * Construit l'exception en conservant l'erreur technique d'origine.
     *
     * <p>À employer depuis un bloc {@code catch} : la cause reste attachée à la trace, sans
     * quoi le diagnostic perd l'appel distant qui a réellement échoué.</p>
     *
     * <p><b>Exemple :</b> {@code new ResourceNotFoundException("Aucune ressource avec l'id: 42",
     * ex)} lorsqu'un appel à un autre microservice répond 404.</p>
     *
     * @param message description de la ressource introuvable
     * @param cause   erreur technique à l'origine de l'échec, conservée pour le diagnostic
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
