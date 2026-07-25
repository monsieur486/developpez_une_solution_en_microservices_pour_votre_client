package com.mr486.mswebclient.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mr486.mswebclient.dto.ErrorMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * Extrait le message d'erreur normalisé contenu dans le message d'une exception
 * d'appel REST.
 *
 * <p><b>Exemple :</b> getErrorMessage("404 : {\"status\":404,\"message\":\"x\"}",
 * "ms-patients") retourne un ErrorMessage de statut 404 ; un message illisible
 * produit un ErrorMessage 503 de repli.</p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ErrorResponseTools {

    private final ObjectMapper objectMapper;

    /**
     * Décode le corps JSON embarqué dans le message d'une exception REST.
     *
     * <p><b>Exemple :</b> un message contenant {"status":404,"message":"absent"}
     * retourne l'ErrorMessage correspondant ; sans JSON exploitable, retourne un
     * ErrorMessage 503 indiquant que le microservice ne répond pas.</p>
     *
     * @param exceptionMessage le message de l'exception levée par l'appel REST
     * @param microserviceName le nom du microservice cité dans le message de repli
     * @return le message d'erreur décodé, ou un message de repli
     */
    public ErrorMessage getErrorMessage(String exceptionMessage, String microserviceName) {
        try {
            int firstBrace = exceptionMessage.indexOf('{');
            int lastBrace = exceptionMessage.lastIndexOf('}');
            String json = exceptionMessage.substring(firstBrace, lastBrace + 1);
            return objectMapper.readValue(json, ErrorMessage.class);
        } catch (Exception e) {
            log.warn("message d'erreur illisible pour {} : repli sur un message générique", microserviceName);
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
            errorMessage.setMessage(microserviceName + " ne répond pas.");
            return errorMessage;
        }
    }
}
