package com.mr486.mswebclient.controller;

import com.mr486.mswebclient.configuration.ConstantesToast;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Place les notifications (« toasts ») dans le modèle ou les attributs flash des pages.
 *
 * <p>Regroupe en un seul endroit l'écriture du couple clé de niveau / clé de message, que les
 * contrôleurs de formulaire répétaient à chaque doublon refusé et à chaque enregistrement réussi.</p>
 *
 * <p><b>Exemple :</b> {@code Notifications.confirme(redirection, "Patient créé.")} affiche une
 * confirmation verte sur la page suivante.</p>
 */
final class Notifications {

    private Notifications() {
    }

    /**
     * Affiche un avertissement sur la page réaffichée.
     *
     * <p><b>Exemple :</b> un doublon refusé réaffiche le formulaire avec « Le patient existe déjà
     * dans la base de données. » en orange.</p>
     *
     * @param model   modèle de la page réaffichée
     * @param message texte de l'avertissement
     */
    static void avertit(Model model, String message) {
        model.addAttribute(ConstantesToast.CLE_NIVEAU, ConstantesToast.NIVEAU_AVERTISSEMENT);
        model.addAttribute(ConstantesToast.CLE_MESSAGE, message);
    }

    /**
     * Affiche une confirmation sur la page atteinte après redirection.
     *
     * <p><b>Exemple :</b> après une mise à jour, la fiche s'ouvre avec « Fiche patient mise à
     * jour. » en vert.</p>
     *
     * @param redirection attributs transmis à la page suivante
     * @param message     texte de la confirmation
     */
    static void confirme(RedirectAttributes redirection, String message) {
        redirection.addFlashAttribute(ConstantesToast.CLE_NIVEAU, ConstantesToast.NIVEAU_INFO);
        redirection.addFlashAttribute(ConstantesToast.CLE_MESSAGE, message);
    }
}
