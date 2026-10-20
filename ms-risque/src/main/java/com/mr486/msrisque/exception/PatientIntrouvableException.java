package com.mr486.msrisque.exception;

import com.mr486.commun.exception.ResourceNotFoundException;

/**
 * Levée quand {@code ms-patients} ne connaît pas le patient à évaluer.
 *
 * <p>Elle hérite de {@code ResourceNotFoundException} : le gestionnaire d'erreurs commun la traduit
 * en réponse 404, avec le message ci-dessous. La cause est l'erreur Feign d'origine.</p>
 *
 * <p><b>Exemple :</b> évaluer le patient 99 donne le message « Aucun patient avec l'id: 99 ».</p>
 */
public class PatientIntrouvableException extends ResourceNotFoundException {
    private static final long serialVersionUID = 1L;

    /**
     * Crée l'exception pour un identifiant donné.
     *
     * <p><b>Exemple :</b> {@code new PatientIntrouvableException(99L, ex)}.</p>
     *
     * @param id identifiant du patient introuvable
     * @param cause erreur Feign renvoyée par {@code ms-patients}
     */
    public PatientIntrouvableException(Long id, Throwable cause) {
        super("Aucun patient avec l'id: " + id, cause);
    }
}
