package com.mr486.msrisque.configuration;

import com.mr486.msrisque.dto.ErrorResponse;
import com.mr486.msrisque.exception.RemoteServiceException;
import io.netty.channel.ChannelOption;
import java.time.Duration;
import java.util.Optional;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.reactive.ReactorLoadBalancerExchangeFilterFunction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

/**
 * Configuration des clients WebClient vers les microservices : authentification
 * Basic sortante, délais courts, traduction des erreurs distantes et répartition
 * de charge optionnelle.
 *
 * <p>Deux modes de déploiement : avec {@code clients.load-balanced=true} (défaut),
 * l'hôte de l'URL est un nom de service résolu par l'annuaire Eureka
 * (docker-compose) ; à {@code false}, l'URL est fixe et la répartition est
 * assurée par la plateforme (Services Kubernetes).</p>
 *
 * <p><b>Exemple :</b> un appel du client {@code patientsWebClient} part avec un
 * en-tête "Authorization: Basic …" ; une réponse en erreur est convertie en
 * {@link RemoteServiceException}.</p>
 */
@Configuration
public class WebClientConfiguration {

    /** Délai maximal de connexion puis de réponse des microservices (en millisecondes). */
    private static final int DELAI_MS = 2000;

    /** Message de repli lorsque la réponse d'erreur distante est illisible. */
    private static final String MESSAGE_REPLI = "Le service ne répond pas. Veuillez réessayer plus tard.";

    @Value("${security.app-user.username}")
    private String user;

    @Value("${security.app-user.password}")
    private String password;

    @Value("${clients.ms-patients.url:http://ms-patients}")
    private String urlPatients;

    @Value("${clients.ms-notes.url:http://ms-notes}")
    private String urlNotes;

    @Value("${clients.load-balanced:true}")
    private boolean loadBalanced;

    /**
     * Client WebClient vers le microservice ms-patients.
     *
     * <p><b>Exemple :</b> patientsWebClient.get().uri("/patients/7") appelle
     * GET /patients/7 sur ms-patients.</p>
     *
     * @param builder  le constructeur WebClient fourni par Spring Boot
     * @param lbFilter le filtre de répartition de charge (utilisé si activé)
     * @return le client configuré pour ms-patients
     */
    @Bean
    public WebClient patientsWebClient(WebClient.Builder builder,
                                       ObjectProvider<ReactorLoadBalancerExchangeFilterFunction> lbFilter) {
        return construitClient(builder, urlPatients, lbFilter);
    }

    /**
     * Client WebClient vers le microservice ms-notes.
     *
     * <p><b>Exemple :</b> notesWebClient.get().uri("/patients/7/notes") appelle
     * GET /patients/7/notes sur ms-notes.</p>
     *
     * @param builder  le constructeur WebClient fourni par Spring Boot
     * @param lbFilter le filtre de répartition de charge (utilisé si activé)
     * @return le client configuré pour ms-notes
     */
    @Bean
    public WebClient notesWebClient(WebClient.Builder builder,
                                    ObjectProvider<ReactorLoadBalancerExchangeFilterFunction> lbFilter) {
        return construitClient(builder, urlNotes, lbFilter);
    }

    // Assemble un client : URL de base, auth Basic, délais, erreurs, répartition éventuelle.
    private WebClient construitClient(WebClient.Builder builder, String baseUrl,
                                      ObjectProvider<ReactorLoadBalancerExchangeFilterFunction> lbFilter) {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, DELAI_MS)
                .responseTimeout(Duration.ofMillis(DELAI_MS));
        WebClient.Builder clientBuilder = builder.clone()
                .baseUrl(baseUrl)
                .defaultHeaders(headers -> headers.setBasicAuth(user, password))
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .filter(filtreErreurDistante());
        if (loadBalanced) {
            clientBuilder.filter(lbFilter.getObject());
        }
        return clientBuilder.build();
    }

    // Convertit toute réponse en erreur en RemoteServiceException (corps décodé ou repli).
    private ExchangeFilterFunction filtreErreurDistante() {
        return ExchangeFilterFunction.ofResponseProcessor(response -> {
            if (!response.statusCode().isError()) {
                return Mono.just(response);
            }
            return response.bodyToMono(ErrorResponse.class)
                    .map(corps -> exceptionDistante(corps, response))
                    .onErrorResume(e -> Mono.just(exceptionRepli(response)))
                    .switchIfEmpty(Mono.just(exceptionRepli(response)))
                    .flatMap(Mono::error);
        });
    }

    // Construit l'exception à partir du corps d'erreur décodé.
    private RemoteServiceException exceptionDistante(ErrorResponse corps, ClientResponse response) {
        int statut = Optional.ofNullable(corps.getStatus()).orElse(response.statusCode().value());
        return new RemoteServiceException(corps.getMessage(), statut, corps);
    }

    // Construit l'exception de repli quand le corps d'erreur est illisible ou vide.
    private RemoteServiceException exceptionRepli(ClientResponse response) {
        ErrorResponse corps = ErrorResponse.builder()
                .status(response.statusCode().value())
                .message(MESSAGE_REPLI)
                .build();
        return new RemoteServiceException(MESSAGE_REPLI, response.statusCode().value(), corps);
    }
}
