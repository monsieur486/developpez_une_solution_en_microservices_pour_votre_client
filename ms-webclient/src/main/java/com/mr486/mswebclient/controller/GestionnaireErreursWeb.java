package com.mr486.mswebclient.controller;

import com.mr486.commun.exception.DuplicateException;
import com.mr486.commun.exception.ResourceNotFoundException;
import com.mr486.commun.exception.ServiceIndisponibleException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Traduit les exceptions des contrôleurs en page d'erreur lisible pour le praticien.
 *
 * <p>Chaque cas fixe le statut HTTP, un titre, un message et indique s'il est utile de
 * réessayer : seule une indisponibilité passagère le justifie.</p>
 *
 * <p><b>Exemple :</b> une fiche introuvable affiche la page {@code erreur} en 404 avec le motif
 * renvoyé par {@code ms-patients} ; une gateway injoignable l'affiche en 503 avec un bouton
 * « Réessayer ».</p>
 */
@ControllerAdvice
@Slf4j
public class GestionnaireErreursWeb {
    private static final String VUE_ERREUR = "erreur";

    private static final String MESSAGE_ERREUR_INTERNE =
            "Une erreur inattendue est survenue. L'équipe technique a été informée.";

    private static final String ATTRIBUT_TITRE = "titre";

    private static final String ATTRIBUT_MESSAGE = "message";

    private static final String ATTRIBUT_REESSAYABLE = "reessayable";

    /**
     * Affiche une ressource introuvable (404).
     *
     * <p><b>Exemple :</b> {@code /app/patients/999} affiche « Aucun patient avec l'id: 999 ».</p>
     *
     * @param ex    exception portant le motif à afficher
     * @param model modèle de la page d'erreur
     * @return la vue {@code erreur}
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String ressourceIntrouvable(ResourceNotFoundException ex, Model model) {
        log.warn("ressource demandée introuvable : {}", ex.getMessage());
        model.addAttribute(ATTRIBUT_TITRE, "Page introuvable");
        model.addAttribute(ATTRIBUT_MESSAGE, ex.getMessage());
        model.addAttribute(ATTRIBUT_REESSAYABLE, false);
        return VUE_ERREUR;
    }

    /**
     * Affiche un enregistrement refusé car déjà existant (409).
     *
     * <p>Filet de sécurité : les formulaires interceptent eux-mêmes le doublon pour se réafficher.</p>
     *
     * <p><b>Exemple :</b> un doublon non intercepté affiche « Enregistrement déjà existant ».</p>
     *
     * @param ex    exception portant le motif renvoyé par le microservice
     * @param model modèle de la page d'erreur
     * @return la vue {@code erreur}
     */
    @ExceptionHandler(DuplicateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String doublon(DuplicateException ex, Model model) {
        log.warn("enregistrement en doublon refusé : {}", ex.getMessage());
        model.addAttribute(ATTRIBUT_TITRE, "Enregistrement déjà existant");
        model.addAttribute(ATTRIBUT_MESSAGE, ex.getMessage());
        model.addAttribute(ATTRIBUT_REESSAYABLE, false);
        return VUE_ERREUR;
    }

    /**
     * Affiche un microservice injoignable (503), en proposant de réessayer.
     *
     * <p><b>Exemple :</b> {@code ms-patients} arrêté affiche « Le service ms-patients est
     * momentanément indisponible. ».</p>
     *
     * @param ex    exception nommant le service injoignable
     * @param model modèle de la page d'erreur
     * @return la vue {@code erreur}
     */
    @ExceptionHandler(ServiceIndisponibleException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public String serviceIndisponible(ServiceIndisponibleException ex, Model model) {
        log.error("service {} injoignable depuis l'interface web", ex.getServiceAppele(), ex);
        model.addAttribute(ATTRIBUT_TITRE, "Service momentanément indisponible");
        model.addAttribute(ATTRIBUT_MESSAGE, ex.getMessage());
        model.addAttribute(ATTRIBUT_REESSAYABLE, true);
        return VUE_ERREUR;
    }

    /**
     * Affiche une page ou un fichier statique inexistant (404).
     *
     * <p><b>Exemple :</b> {@code /page-inconnue} affiche « La ressource demandée est introuvable. ».</p>
     *
     * @param ex    exception portant le chemin demandé
     * @param model modèle de la page d'erreur
     * @return la vue {@code erreur}
     */
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String ressourceStatiqueIntrouvable(NoResourceFoundException ex, Model model) {
        log.warn("ressource statique absente : {}", ex.getResourcePath());
        model.addAttribute(ATTRIBUT_TITRE, "Page introuvable");
        model.addAttribute(ATTRIBUT_MESSAGE, "La ressource demandée est introuvable.");
        model.addAttribute(ATTRIBUT_REESSAYABLE, false);
        return VUE_ERREUR;
    }

    /**
     * Affiche toute autre erreur (500) sans détail technique.
     *
     * <p>Couvre aussi les appels refusés par un microservice ({@code AppelRefuseException}) : ce sont
     * des défauts de configuration, réessayer n'y changerait rien.</p>
     *
     * <p><b>Exemple :</b> des identifiants techniques erronés (401 de la gateway) affichent « Erreur
     * inattendue » ; la cause complète reste dans les journaux.</p>
     *
     * @param ex    exception non anticipée
     * @param model modèle de la page d'erreur
     * @return la vue {@code erreur}
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String erreurInattendue(Exception ex, Model model) {
        log.error("erreur inattendue dans l'interface web", ex);
        model.addAttribute(ATTRIBUT_TITRE, "Erreur inattendue");
        model.addAttribute(ATTRIBUT_MESSAGE, MESSAGE_ERREUR_INTERNE);
        model.addAttribute(ATTRIBUT_REESSAYABLE, false);
        return VUE_ERREUR;
    }
}
