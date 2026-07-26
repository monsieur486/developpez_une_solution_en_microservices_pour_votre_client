package com.mr486.mswebclient.configuration;

import com.mr486.mswebclient.dto.ErrorMessage;
import com.mr486.mswebclient.exception.GatewayException;
import io.netty.channel.ChannelOption;
import java.time.Duration;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

/**
 * Configuration du client REST réactif vers la passerelle : authentification
 * Basic sortante, délais courts et traduction des erreurs distantes.
 *
 * <p><b>Exemple :</b> chaque appel du WebClient part avec un en-tête
 * "Authorization: Basic …" ; une réponse en erreur est convertie en
 * {@link GatewayException} portant le message à afficher.</p>
 */
@Configuration
public class GatewayClientConfiguration {

    /** Délai maximal de connexion puis de réponse de la passerelle (en millisecondes). */
    private static final int DELAI_MS = 5000;

    /** Message de repli lorsque la réponse d'erreur distante est illisible. */
    private static final String MESSAGE_REPLI = "Le service ne répond pas. Veuillez réessayer plus tard.";

    @Value("${app.gateway.base-url}")
    private String gatewayBase;

    @Value("${app.auth.username}")
    private String username;

    @Value("${app.auth.password}")
    private String password;

    /**
     * Client WebClient authentifié utilisé pour appeler la passerelle.
     *
     * <p><b>Exemple :</b> gatewayWebClient.get().uri("/ms-patients/patients")
     * appelle la liste des patients au travers de la passerelle.</p>
     *
     * @param builder le constructeur WebClient fourni par Spring Boot
     * @return le client REST réactif authentifié
     */
    @Bean
    public WebClient gatewayWebClient(WebClient.Builder builder) {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, DELAI_MS)
                .responseTimeout(Duration.ofMillis(DELAI_MS));
        return builder
                .baseUrl(gatewayBase)
                .defaultHeaders(headers -> headers.setBasicAuth(username, password))
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .filter(filtreErreurDistante())
                .build();
    }

    // Convertit toute réponse en erreur en GatewayException (corps décodé ou repli).
    private ExchangeFilterFunction filtreErreurDistante() {
        return ExchangeFilterFunction.ofResponseProcessor(response -> {
            if (!response.statusCode().isError()) {
                return Mono.just(response);
            }
            return response.bodyToMono(ErrorMessage.class)
                    .map(corps -> exceptionDistante(corps, response))
                    .onErrorResume(e -> Mono.just(exceptionRepli(response)))
                    .switchIfEmpty(Mono.just(exceptionRepli(response)))
                    .flatMap(Mono::error);
        });
    }

    // Construit l'exception à partir du corps d'erreur décodé (repli si champs absents).
    private GatewayException exceptionDistante(ErrorMessage corps, ClientResponse response) {
        corps.setStatus(Optional.ofNullable(corps.getStatus()).orElse(response.statusCode().value()));
        if (corps.getMessage() == null || corps.getMessage().isBlank()) {
            corps.setMessage(MESSAGE_REPLI);
        }
        return new GatewayException(corps);
    }

    // Construit l'exception de repli quand le corps d'erreur est illisible ou vide.
    private GatewayException exceptionRepli(ClientResponse response) {
        return new GatewayException(new ErrorMessage(response.statusCode().value(), MESSAGE_REPLI));
    }
}
