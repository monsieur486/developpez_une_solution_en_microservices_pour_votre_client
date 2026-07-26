package com.mr486.msnotes.configuration;

/**
 * Constantes générales de l'application (invariants de domaine, hors
 * configuration d'environnement).
 *
 * <p><b>Exemple :</b> ConstantesApplication.TAILLE_PAGE_NOTES vaut 5 : les
 * notes paginées sont servies par pages de 5, de la plus récente à la plus
 * ancienne.</p>
 */
public final class ConstantesApplication {

    /** Taille de page des notes paginées (les 5 dernières notes par page). */
    public static final int TAILLE_PAGE_NOTES = 5;

    private ConstantesApplication() {
        // classe utilitaire : pas d'instanciation
    }
}
