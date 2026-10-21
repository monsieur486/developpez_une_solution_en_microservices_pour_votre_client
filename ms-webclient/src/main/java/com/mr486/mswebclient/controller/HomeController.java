package com.mr486.mswebclient.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Sert les pages publiques : accueil et connexion.
 *
 * <p><b>Exemple :</b> {@code GET /} affiche l'accueil sans authentification.</p>
 */
@Controller
public class HomeController {
    /**
     * Affiche la page d'accueil.
     *
     * <p><b>Exemple :</b> {@code GET /} et {@code GET /home} affichent la même page.</p>
     *
     * @return la vue {@code home}
     */
    @GetMapping({"/", "/home"})
    public String home() {
        return "home";
    }

    /**
     * Affiche le formulaire de connexion ; sa soumission est traitée par Spring Security.
     *
     * <p><b>Exemple :</b> {@code GET /login?error} réaffiche le formulaire avec un message d'échec.</p>
     *
     * @return la vue {@code login}
     */
    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
