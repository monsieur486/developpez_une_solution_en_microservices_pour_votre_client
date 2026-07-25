package com.mr486.mswebclient.service;

import com.mr486.mswebclient.dto.ErrorMessage;
import com.mr486.mswebclient.dto.Risque;
import com.mr486.mswebclient.exception.GatewayException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Accède à l'évaluation du risque calculée par ms-risque au travers de la
 * passerelle, sans bloquer.
 *
 * <p><b>Exemple :</b> getEvaluationByPatientId(7L) émet le risque calculé
 * pour le patient 7.</p>
 */
@Service
public class EvaluationService {

    /** Nom du microservice cité dans les messages d'erreur de repli. */
    private static final String MICROSERVICE = "ms-risque";

    private final WebClient gatewayWebClient;

    /**
     * Construit le service avec le client REST réactif de la passerelle.
     *
     * <p><b>Exemple :</b> new EvaluationService(gatewayWebClient) appellera
     * /ms-risque/** au travers de la passerelle.</p>
     *
     * @param gatewayWebClient le client REST authentifié vers la passerelle
     */
    public EvaluationService(WebClient gatewayWebClient) {
        this.gatewayWebClient = gatewayWebClient;
    }

    /**
     * Retourne l'évaluation du risque de diabète d'un patient, sans bloquer.
     *
     * <p><b>Exemple :</b> getEvaluationByPatientId(2L) émet un Risque de niveau
     * {@code "Borderline"} pour le patient TestBorderline du jeu de démonstration
     * (décrit dans le README racine).</p>
     *
     * @param patientId identifiant du patient à évaluer
     * @return un Mono émettant le risque calculé pour ce patient
     */
    public Mono<Risque> getEvaluationByPatientId(Long patientId) {
        return gatewayWebClient.get()
                .uri("/ms-risque/patients/{id}/evaluation", patientId)
                .retrieve()
                .bodyToMono(Risque.class)
                .onErrorMap(this::estUneErreurTechnique, e -> repli());
    }

    // Vraie panne (connexion, délai…) : tout sauf une erreur distante déjà traduite.
    private boolean estUneErreurTechnique(Throwable e) {
        return !(e instanceof GatewayException);
    }

    // Message de repli nominatif affiché quand la passerelle est injoignable.
    private GatewayException repli() {
        return new GatewayException(
                new ErrorMessage(HttpStatus.SERVICE_UNAVAILABLE.value(), MICROSERVICE + " ne répond pas."));
    }
}
