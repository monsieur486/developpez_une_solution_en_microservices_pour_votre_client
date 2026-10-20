package com.mr486.commun.exception;

/**
 * Signale qu'un microservice appelé est injoignable ou ne répond plus.
 *
 * <p>Levée lorsqu'un appel entre services échoue pour une cause réseau — service arrêté,
 * délai d'attente dépassé, aucune instance enregistrée auprès du registre — et non parce
 * que la donnée demandée est absente. Les gestionnaires d'exception la traduisent en
 * réponse HTTP 503, qui indique à l'appelant que la demande pourra aboutir plus tard.</p>
 *
 * <p><b>Exemple :</b> un appel vers un microservice arrêté lève
 * {@code new ServiceIndisponibleException("ms-cible", cause)} et produit le message
 * « Le service ms-cible est momentanément indisponible. ».</p>
 */
public class ServiceIndisponibleException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** Gabarit du message présenté à l'appelant, complété par le nom du service. */
    private static final String MESSAGE = "Le service %s est momentanément indisponible.";

    /** Nom du microservice injoignable, tel qu'enregistré auprès du registre de services. */
    private final String serviceAppele;

    /**
     * Construit l'exception pour le microservice injoignable désigné.
     *
     * <p><b>Exemple :</b> {@code new ServiceIndisponibleException("ms-cible", cause)}
     * porte le message « Le service ms-cible est momentanément indisponible. ».</p>
     *
     * @param serviceAppele nom du microservice qui n'a pas répondu
     * @param cause         erreur technique à l'origine de l'échec, conservée pour le diagnostic
     */
    public ServiceIndisponibleException(String serviceAppele, Throwable cause) {
        super(String.format(MESSAGE, serviceAppele), cause);
        this.serviceAppele = serviceAppele;
    }

    /**
     * Retourne le nom du microservice qui n'a pas répondu.
     *
     * <p><b>Exemple :</b> {@code getServiceAppele()} retourne {@code "ms-cible"} lorsque
     * ce service est arrêté.</p>
     *
     * @return le nom du microservice injoignable
     */
    public String getServiceAppele() {
        return serviceAppele;
    }
}
