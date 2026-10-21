package com.mr486.commun.exception;

/**
 * Signale qu'un microservice a refusé la requête qu'on lui adressait.
 *
 * <p>Levée lorsqu'un appel entre services reçoit une réponse 4xx autre que 404 ou 409 —
 * typiquement 400, 401 ou 403. Contrairement à {@link ServiceIndisponibleException}, le
 * service répond : c'est la requête elle-même qui est en cause (données refusées,
 * identifiants techniques erronés, droits insuffisants). Réessayer plus tard ne changerait
 * rien ; il s'agit d'un défaut de l'appelant à corriger.</p>
 *
 * <p><b>Exemple :</b> un appel vers {@code ms-cible} refusé en 403 lève
 * {@code new AppelRefuseException("ms-cible", 403, cause)} et produit le message
 * « Le service ms-cible a refusé la requête (statut 403). ».</p>
 */
public class AppelRefuseException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** Gabarit du message, complété par le nom du service et le statut HTTP reçu. */
    private static final String MESSAGE = "Le service %s a refusé la requête (statut %d).";

    /** Statut HTTP renvoyé par le microservice, conservé pour le diagnostic. */
    private final int statut;

    /**
     * Construit l'exception pour le microservice qui a refusé la requête.
     *
     * <p><b>Exemple :</b> {@code new AppelRefuseException("ms-cible", 401, cause)} porte le
     * message « Le service ms-cible a refusé la requête (statut 401). ».</p>
     *
     * @param serviceAppele nom du microservice qui a refusé la requête
     * @param statut        statut HTTP 4xx renvoyé par le microservice
     * @param cause         erreur technique à l'origine du refus, conservée pour le diagnostic
     */
    public AppelRefuseException(String serviceAppele, int statut, Throwable cause) {
        super(String.format(MESSAGE, serviceAppele, statut), cause);
        this.statut = statut;
    }

    /**
     * Retourne le statut HTTP renvoyé par le microservice.
     *
     * <p><b>Exemple :</b> {@code getStatut()} retourne {@code 403} lorsque les identifiants
     * techniques n'ont pas les droits requis.</p>
     *
     * @return le statut HTTP 4xx reçu
     */
    public int getStatut() {
        return statut;
    }
}
