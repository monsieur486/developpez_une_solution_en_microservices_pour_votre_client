package com.mr486.mswebclient.controller;

import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;

/**
 * Normalise les textes saisis dans tous les formulaires de l'interface web.
 *
 * <p>Un navigateur envoie toujours les champs vides sous forme de chaîne vide : sans ce
 * traitement, un patient sans téléphone serait enregistré avec un téléphone {@code ""} au lieu
 * de {@code null}.</p>
 *
 * <p><b>Exemple :</b> le champ téléphone laissé vide arrive {@code null} au contrôleur ;
 * {@code "  Dupont "} arrive {@code "Dupont"}.</p>
 */
@ControllerAdvice
public class LiaisonFormulaires {

    /**
     * Retire les espaces en bordure des textes et convertit les textes vides en {@code null}.
     *
     * <p><b>Exemple :</b> {@code "   "} devient {@code null}, {@code " Test "} devient {@code "Test"}.</p>
     *
     * @param liaison liaison des paramètres de la requête au formulaire
     */
    @InitBinder
    public void normaliseLesTextes(WebDataBinder liaison) {
        liaison.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }
}
