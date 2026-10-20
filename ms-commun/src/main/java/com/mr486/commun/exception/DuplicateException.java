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
}
