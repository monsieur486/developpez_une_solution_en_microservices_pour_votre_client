package com.mr486.mswebclient.configuration;

/**
 * Noms et chemins des microservices appelés à travers la gateway.
 *
 * <p>Le préfixe {@code /ms-patients} est le nom du service dans Eureka : la gateway le retire
 * avant de transmettre la requête.</p>
 *
 * <p><b>Exemple :</b> {@code CHEMIN_PATIENTS + "/1"} vise {@code GET /patients/1} de {@code ms-patients}.</p>
 */
public final class ConstantesWebclient {
    /** Nom du microservice des patients, repris dans les messages d'erreur. */
    public static final String SERVICE_PATIENTS = "ms-patients";

    public static final String SERVICE_NOTES = "ms-notes";

    /** Chemin de l'API des patients à partir de la gateway. */
    public static final String CHEMIN_PATIENTS = "/ms-patients/patients";

    public static final String CHEMIN_NOTES = "/ms-notes/patients/";

    private ConstantesWebclient() {
    }
}
