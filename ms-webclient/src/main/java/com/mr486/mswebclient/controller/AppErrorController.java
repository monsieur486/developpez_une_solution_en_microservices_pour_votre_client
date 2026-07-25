package com.mr486.mswebclient.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Pages d'erreur de l'application : choisit la vue selon le code HTTP.
 *
 * <p><b>Exemple :</b> une erreur 404 affiche la vue "errors/error-404" ; une
 * erreur 500 affiche "errors/error-500".</p>
 */
@Controller
public class AppErrorController implements ErrorController {

    /**
     * Affiche la page d'erreur correspondant au code HTTP de la requête.
     *
     * <p><b>Exemple :</b> une requête portant le statut 404 retourne la vue
     * "errors/error-404" ; un statut inconnu retourne "errors/error".</p>
     *
     * @param request la requête portant l'attribut de code d'erreur
     * @return le nom de la vue d'erreur adaptée au statut
     */
    @RequestMapping("/error")
    public String handleError(HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());

            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                return "errors/error-404";
            } else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                return "errors/error-500";
            }
        }
        return "errors/error";
    }
}
