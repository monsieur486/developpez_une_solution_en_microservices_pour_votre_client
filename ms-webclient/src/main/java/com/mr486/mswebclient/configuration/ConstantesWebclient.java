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

    /** Nom du microservice des notes, repris dans les messages d'erreur. */
    public static final String SERVICE_NOTES = "ms-notes";

    /** Nom du microservice d'évaluation du risque, repris dans les messages d'erreur. */
    public static final String SERVICE_RISQUE = "ms-risque";

    /** Chemin de l'API des patients à partir de la gateway. */
    public static final String CHEMIN_PATIENTS = "/ms-patients/patients";

    /** Début du chemin de l'API des notes à partir de la gateway, suivi de l'id du patient. */
    public static final String CHEMIN_NOTES = "/ms-notes/patients/";

    /** Début du chemin de l'évaluation du risque à partir de la gateway, suivi de l'id du patient. */
    public static final String CHEMIN_EVALUATION = "/ms-risque/evaluation/";

    private ConstantesWebclient() {
    }
}
