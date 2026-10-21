package com.mr486.commun.exception;

/**
 * Signale qu'un enregistrement identique existe déjà.
 *
 * <p>Levée par la couche service lorsqu'une création ou une modification violerait une
 * règle d'unicité ; les gestionnaires d'exception la traduisent en réponse
 * HTTP 409. L'opération demandée n'est pas appliquée.</p>
 *
 * <p><b>Exemple :</b> {@code throw new DuplicateException("La ressource existe déjà.")}
 * lorsqu'un enregistrement portant la même clé d'unicité est déjà présent.</p>
 */
public class DuplicateException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Construit l'exception avec le message présenté à l'appelant.
     *
     * <p><b>Exemple :</b> {@code new DuplicateException("La ressource existe déjà.")}.</p>
     *
     * @param message description du conflit détecté
     */
    public DuplicateException(String message) {
        super(message);
    }

    /**
     * Construit l'exception en conservant l'erreur technique d'origine.
     *
     * <p>À employer depuis un bloc {@code catch} : la cause reste attachée à la trace, sans
     * quoi le diagnostic perd l'appel distant qui a réellement échoué.</p>
     *
     * <p><b>Exemple :</b> {@code new DuplicateException("La ressource existe déjà.", ex)}
     * lorsqu'un appel à un autre microservice répond 409.</p>
     *
     * @param message description du conflit détecté
     * @param cause   erreur technique à l'origine de l'échec, conservée pour le diagnostic
     */
    public DuplicateException(String message, Throwable cause) {
        super(message, cause);
    }
}
