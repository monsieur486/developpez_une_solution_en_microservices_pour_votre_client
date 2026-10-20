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

@ControllerAdvice
@Slf4j
public class GestionnaireErreursWeb {
    private static final String VUE_ERREUR = "erreur";

    private static final String MESSAGE_ERREUR_INTERNE =
            "Une erreur inattendue est survenue. L'équipe technique a été informée.";

    private static final String ATTRIBUT_TITRE = "titre";

    private static final String ATTRIBUT_MESSAGE = "message";

    private static final String ATTRIBUT_REESSAYABLE = "reessayable";

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String ressourceIntrouvable(ResourceNotFoundException ex, Model model) {
        log.warn("ressource demandée introuvable : {}", ex.getMessage());
        model.addAttribute(ATTRIBUT_TITRE, "Page introuvable");
        model.addAttribute(ATTRIBUT_MESSAGE, ex.getMessage());
        model.addAttribute(ATTRIBUT_REESSAYABLE, false);
        return VUE_ERREUR;
    }

    @ExceptionHandler(DuplicateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String doublon(DuplicateException ex, Model model) {
        log.warn("enregistrement en doublon refusé : {}", ex.getMessage());
        model.addAttribute(ATTRIBUT_TITRE, "Enregistrement déjà existant");
        model.addAttribute(ATTRIBUT_MESSAGE, ex.getMessage());
        model.addAttribute(ATTRIBUT_REESSAYABLE, false);
        return VUE_ERREUR;
    }

    @ExceptionHandler(ServiceIndisponibleException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public String serviceIndisponible(ServiceIndisponibleException ex, Model model) {
        log.error("service {} injoignable depuis l'interface web", ex.getServiceAppele(), ex);
        model.addAttribute(ATTRIBUT_TITRE, "Service momentanément indisponible");
        model.addAttribute(ATTRIBUT_MESSAGE, ex.getMessage());
        model.addAttribute(ATTRIBUT_REESSAYABLE, true);
        return VUE_ERREUR;
    }

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String ressourceStatiqueIntrouvable(NoResourceFoundException ex, Model model) {
        log.warn("ressource statique absente : {}", ex.getResourcePath());
        model.addAttribute(ATTRIBUT_TITRE, "Page introuvable");
        model.addAttribute(ATTRIBUT_MESSAGE, "La ressource demandée est introuvable.");
        model.addAttribute(ATTRIBUT_REESSAYABLE, false);
        return VUE_ERREUR;
    }

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
