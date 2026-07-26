package com.mr486.mswebclient.service;

import com.mr486.mswebclient.dto.ErrorMessage;
import com.mr486.mswebclient.dto.PageReponse;
import com.mr486.mswebclient.dto.Patient;
import com.mr486.mswebclient.dto.PatientForm;
import com.mr486.mswebclient.exception.GatewayException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Accède aux patients exposés par ms-patients au travers de la passerelle,
 * sans bloquer.
 *
 * <p><b>Exemple :</b> getPatients(0) émet la première page de patients ;
 * createPatient(form) crée un nouveau patient.</p>
 */
@Service
public class PatientService {

    /** Nom du microservice cité dans les messages d'erreur de repli. */
    private static final String MICROSERVICE = "ms-patients";

    private final WebClient gatewayWebClient;

    /**
     * Construit le service avec le client REST réactif de la passerelle.
     *
     * <p><b>Exemple :</b> new PatientService(gatewayWebClient) appellera
     * /ms-patients/** au travers de la passerelle.</p>
     *
     * @param gatewayWebClient le client REST authentifié vers la passerelle
     */
    public PatientService(WebClient gatewayWebClient) {
        this.gatewayWebClient = gatewayWebClient;
    }

    /**
     * Retourne une page de patients (20 par page), sans bloquer.
     *
     * <p><b>Exemple :</b> getPatients(0) émet la première page de patients avec
     * ses informations de navigation ; une panne de la passerelle propage une
     * {@link GatewayException}.</p>
     *
     * @param page numéro de la page demandée (à partir de 0)
     * @return un Mono émettant la page de patients
     */
    public Mono<PageReponse<Patient>> getPatients(int page) {
        return gatewayWebClient.get()
                .uri("/ms-patients/patients?page={page}", page)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<PageReponse<Patient>>() {
                })
                .onErrorMap(this::estUneErreurTechnique, e -> repli());
    }

    /**
     * Retourne un patient par son identifiant, sans bloquer.
     *
     * <p><b>Exemple :</b> getPatientById(7L) émet le patient d'identifiant 7.</p>
     *
     * @param id identifiant du patient
     * @return un Mono émettant le patient correspondant
     */
    public Mono<Patient> getPatientById(Long id) {
        return gatewayWebClient.get()
                .uri("/ms-patients/patients/{id}", id)
                .retrieve()
                .bodyToMono(Patient.class)
                .onErrorMap(this::estUneErreurTechnique, e -> repli());
    }

    /**
     * Crée un nouveau patient, sans bloquer.
     *
     * <p><b>Exemple :</b> createPatient(form) envoie le formulaire à ms-patients
     * qui persiste le patient.</p>
     *
     * @param patient le formulaire du patient à créer
     * @return un Mono complété quand la création est faite
     */
    public Mono<Void> createPatient(PatientForm patient) {
        return gatewayWebClient.post()
                .uri("/ms-patients/patients")
                .bodyValue(patient)
                .retrieve()
                .bodyToMono(Void.class)
                .onErrorMap(this::estUneErreurTechnique, e -> repli());
    }

    /**
     * Met à jour un patient existant, sans bloquer.
     *
     * <p><b>Exemple :</b> updatePatient(7L, form) remplace les données du patient
     * 7 par celles du formulaire.</p>
     *
     * @param id      identifiant du patient à modifier
     * @param patient le formulaire portant les nouvelles données
     * @return un Mono complété quand la mise à jour est faite
     */
    public Mono<Void> updatePatient(Long id, PatientForm patient) {
        return gatewayWebClient.put()
                .uri("/ms-patients/patients/{id}", id)
                .bodyValue(patient)
                .retrieve()
                .bodyToMono(Void.class)
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
