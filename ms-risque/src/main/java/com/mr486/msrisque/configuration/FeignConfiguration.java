package com.mr486.msrisque.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mr486.msrisque.dto.ErrorResponse;
import com.mr486.msrisque.exception.RemoteServiceException;
import feign.RequestInterceptor;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

/**
 * Configuration des clients Feign : authentification Basic sortante et
 * traduction des erreurs distantes en exceptions applicatives.
 *
 * <p><b>Exemple :</b> chaque appel Feign part avec un en-tête
 * "Authorization: Basic …" ; une réponse en erreur est convertie en
 * RemoteServiceException.</p>
 */
@Configuration
public class FeignConfiguration {

    @Value("${security.app-user.username}")
    private String user;

    @Value("${security.app-user.password}")
    private String password;

    /**
     * Intercepteur ajoutant l'authentification Basic aux requêtes Feign.
     *
     * <p><b>Exemple :</b> ajoute l'en-tête "Authorization: Basic dXNlcjpwYXNz"
     * construit à partir des identifiants applicatifs.</p>
     *
     * @return l'intercepteur de requêtes Feign
     */
    @Bean
    public RequestInterceptor resourceBAuthInterceptor() {
        return template -> {
            String token = user + ":" + password;
            String base64 = Base64.getEncoder().encodeToString(token.getBytes(StandardCharsets.UTF_8));
            template.header("Authorization", "Basic " + base64);
        };
    }

    /**
     * Décodeur traduisant les réponses Feign en erreur en RemoteServiceException.
     *
     * <p><b>Exemple :</b> une réponse 404 du service distant produit une
     * RemoteServiceException portant le statut et le message décodés.</p>
     *
     * @param mapper le mapper Jackson utilisé pour lire le corps d'erreur
     * @return le décodeur d'erreurs Feign
     */
    @Bean
    public ErrorDecoder errorDecoder(ObjectMapper mapper) {
        return new ErrorDecoder() {
            @Override
            public Exception decode(String methodKey, Response response) {
                try {
                    Reader reader = response.body().asReader(StandardCharsets.UTF_8);
                    ErrorResponse errorResponse = mapper.readValue(reader, ErrorResponse.class);
                    String message = errorResponse.getMessage();
                    return new RemoteServiceException(message, errorResponse.getStatus(), errorResponse) {
                    };
                } catch (Exception e) {
                    String fallbackMessage = "Le service ne répond pas. Veuillez réessayer plus tard.";
                    ErrorResponse errorResponse = new ErrorResponse();
                    errorResponse.setStatus(response.status());
                    errorResponse.setMessage(Optional.ofNullable(e.getMessage())
                            .orElse("Erreur inconnue."));
                    return new RemoteServiceException(fallbackMessage, response.status(),
                            errorResponse) {
                    };
                }
            }
        };
    }
}
