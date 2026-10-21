package com.mr486.mspatients.exception;

import com.mr486.commun.exception.DuplicateException;

/**
 * Signale qu'un patient de même identité (nom, prénom, date de naissance, genre) existe déjà.
 *
 * <p>Traduite en réponse 409 par le gestionnaire d'erreurs commun.</p>
 *
 * <p><b>Exemple :</b> créer une deuxième fois le patient {@code TestNone} lève cette exception.</p>
 */
public class PatientDejaExistantException extends DuplicateException {
    private static final long serialVersionUID = 1L;

    private static final String MESSAGE = "Le patient existe déjà dans la base de données.";

    /**
     * Construit l'exception avec le message renvoyé à l'appelant.
     *
     * <p><b>Exemple :</b> {@code new PatientDejaExistantException().getMessage()} vaut
     * « Le patient existe déjà dans la base de données. ».</p>
     */
    public PatientDejaExistantException() {
        super(MESSAGE);
    }
}
