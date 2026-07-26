package com.mr486.mspatients.configuration;

/**
 * Constantes générales de l'application (invariants de domaine, hors
 * configuration d'environnement).
 *
 * <p><b>Exemple :</b> ConstantesApplication.TAILLE_PAGE_PATIENTS vaut 20 :
 * la liste des patients est servie par pages de 20.</p>
 */
public final class ConstantesApplication {

    /** Taille de page de la liste des patients (compromis lisibilité / charge). */
    public static final int TAILLE_PAGE_PATIENTS = 20;

    private ConstantesApplication() {
        // classe utilitaire : pas d'instanciation
    }
}
