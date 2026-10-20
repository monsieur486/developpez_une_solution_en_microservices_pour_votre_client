package com.mr486.mspatients.exception;

import com.mr486.commun.exception.ResourceNotFoundException;

/**
 * Signale qu'aucun patient ne porte l'identifiant demandé.
 *
 * <p>Traduite en réponse 404 par le gestionnaire d'erreurs commun.</p>
 *
 * <p><b>Exemple :</b> {@code GET /patients/999} sur une base qui ne le contient pas lève cette exception.</p>
 */
public class PatientIntrouvableException extends ResourceNotFoundException {
    private static final long serialVersionUID = 1L;

    /**
     * Construit l'exception à partir de l'identifiant introuvable.
     *
     * <p><b>Exemple :</b> {@code new PatientIntrouvableException(42L).getMessage()} vaut
     * « Aucun patient avec l'id: 42 ».</p>
     *
     * @param id identifiant recherché sans succès
     */
    public PatientIntrouvableException(Long id) {
        super("Aucun patient avec l'id: " + id);
    }
}
