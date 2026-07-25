package com.mr486.mswebclient.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Pages publiques de l'application : accueil et connexion.
 *
 * <p><b>Exemple :</b> GET / affiche la page d'accueil ; GET /login affiche le
 * formulaire de connexion.</p>
 */
@Controller
public class HomeController {

    /**
     * Affiche la page d'accueil.
     *
     * <p><b>Exemple :</b> GET / ou GET /home retourne la vue "home".</p>
     *
     * @return le nom de la vue d'accueil
     */
    @GetMapping({"/", "/home"})
    public String home() {
        return "home";
    }

    /**
     * Affiche la page de connexion.
     *
     * <p><b>Exemple :</b> GET /login retourne la vue "login".</p>
     *
     * @return le nom de la vue de connexion
     */
    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
