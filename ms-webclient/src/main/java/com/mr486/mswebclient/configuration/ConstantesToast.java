package com.mr486.mswebclient.configuration;

/**
 * Clés et niveaux des notifications (« toasts ») affichées après une action.
 *
 * <p>Les contrôleurs les placent dans le modèle ou en attribut flash ; le fragment
 * {@code fragments/toast.html} les lit pour afficher le message.</p>
 *
 * <p><b>Exemple :</b> {@code redirection.addFlashAttribute(CLE_MESSAGE, "Patient créé.")} avec
 * {@code CLE_NIVEAU} à {@code NIVEAU_INFO} affiche une confirmation sur la page suivante.</p>
 */
public final class ConstantesToast {
    /** Clé du texte de la notification. */
    public static final String CLE_MESSAGE = "toastMessage";

    /** Clé du niveau de la notification, qui fixe sa couleur. */
    public static final String CLE_NIVEAU = "toastNiveau";

    /** Niveau d'une confirmation (action réussie). */
    public static final String NIVEAU_INFO = "info";

    /** Niveau d'un avertissement (action refusée, à corriger par l'utilisateur). */
    public static final String NIVEAU_AVERTISSEMENT = "avertissement";

    /** Niveau d'une erreur. */
    public static final String NIVEAU_ERREUR = "erreur";

    private ConstantesToast() {
    }
}
