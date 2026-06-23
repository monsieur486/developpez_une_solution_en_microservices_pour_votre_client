package com.mr486.mspatients.exception;

/**
 * Exception levée lorsqu'une entité demandée est introuvable.
 *
 * <p><b>Exemple :</b> new NotFoundException("aucun patient trouvé avec l'id: 7")
 * est traduit en réponse HTTP 404 par le gestionnaire d'exceptions.</p>
 */
public class NotFoundException extends RuntimeException {

    /**
     * Construit l'exception avec le message détaillé fourni.
     *
     * <p><b>Exemple :</b> new NotFoundException("introuvable") porte le message
     * "introuvable".</p>
     *
     * @param message le message expliquant la raison de l'exception
     */
    public NotFoundException(String message) {
        super(message);
    }
}
