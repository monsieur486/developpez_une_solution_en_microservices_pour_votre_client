package com.mr486.commun.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Traduit les exceptions des microservices en réponses HTTP uniformes.
 *
 * <p>Le traitement distingue nettement deux familles d'erreurs :</p>
 *
 * <ul>
 *   <li><b>4xx — l'appelant est en cause</b> (donnée invalide, ressource inexistante,
 *       conflit). Le message explicatif lui est renvoyé tel quel pour qu'il puisse corriger
 *       sa demande, et l'incident est journalisé en {@code warn} : le service fonctionne
 *       normalement.</li>
 *   <li><b>5xx — le service est en cause</b> (panne, dépendance injoignable, bug). Seul un
 *       message générique est renvoyé, sans détail technique exploitable de l'extérieur, et
 *       la cause complète est journalisée en {@code error} avec sa pile d'appels.</li>
 * </ul>
 *
 * <p>Cette classe n'est pas détectée automatiquement par les microservices, qui ne
 * balayent que leur propre paquetage : chacun l'active par
 * {@code @Import(GlobalExceptionHandler.class)}.</p>
 *
 * <p><b>Exemple :</b> une demande portant sur une ressource inexistante produit un statut 404
 * et le message « Aucune ressource avec l'id: 42 » ; une panne de base de données produit un
 * statut 500 et le seul message « Une erreur interne est survenue. ».</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    /** Message renvoyé pour toute erreur serveur non anticipée, sans détail technique. */
    private static final String MESSAGE_ERREUR_INTERNE = "Une erreur interne est survenue.";

    /** Journalise chaque erreur et fabrique la réponse normalisée renvoyée au client. */
    private final ReponsesErreur reponses;

    /**
     * Construit le gestionnaire pour le microservice qui l'importe.
     *
     * <p><b>Exemple :</b> importé par {@code ms-patients}, il signe chaque réponse d'erreur
     * {@code "microserviceName": "ms-patients"}.</p>
     *
     * @param nomDuMicroservice valeur de {@code spring.application.name}, {@code application} à défaut
     */
    public GlobalExceptionHandler(
            @Value("${spring.application.name:application}") String nomDuMicroservice) {
        this.reponses = new ReponsesErreur(nomDuMicroservice);
    }

    /**
     * Traduit une ressource introuvable en réponse 404.
     *
     * <p><b>Exemple :</b> une demande portant sur une ressource inexistante retourne un statut
     * 404 et le message « Aucune ressource avec l'id: 42 ».</p>
     *
     * @param ex      exception signalant l'absence de la ressource
     * @param requete requête à l'origine de l'erreur
     * @return la réponse 404 normalisée
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleResourceNotFound(
            ResourceNotFoundException ex, HttpServletRequest requete) {
        return reponses.client(HttpStatus.NOT_FOUND, List.of(ex.getMessage()), requete);
    }

    /**
     * Traduit une violation d'unicité en réponse 409.
     *
     * <p>Le conflit porte sur l'état de la ressource, pas sur la syntaxe de la demande :
     * le statut 409 le signale plus précisément qu'un 400.</p>
     *
     * <p><b>Exemple :</b> enregistrer deux fois la même ressource retourne un statut 409 et
     * le message « La ressource existe déjà. ».</p>
     *
     * @param ex      exception signalant le doublon
     * @param requete requête à l'origine de l'erreur
     * @return la réponse 409 normalisée
     */
    @ExceptionHandler(DuplicateException.class)
    public ResponseEntity<Object> handleDuplication(
            DuplicateException ex, HttpServletRequest requete) {
        return reponses.client(HttpStatus.CONFLICT, List.of(ex.getMessage()), requete);
    }

    /**
     * Traduit les violations de contraintes portées par les paramètres d'URL en réponse 400.
     *
     * <p><b>Exemple :</b> un identifiant négatif retourne un statut 400 et le message
     * « getRessource.id: doit être supérieur à 0 ».</p>
     *
     * @param ex      exception portant les violations détectées
     * @param requete requête à l'origine de l'erreur
     * @return la réponse 400 normalisée
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest requete) {
        List<String> messages = ex.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .toList();
        return reponses.client(HttpStatus.BAD_REQUEST, messages, requete);
    }

    /**
     * Traduit l'indisponibilité d'un microservice appelé en réponse 503.
     *
     * <p>L'appelant reçoit un message explicite — la demande pourra aboutir plus tard —
     * mais aucun détail technique sur la panne.</p>
     *
     * <p><b>Exemple :</b> un appel vers un microservice arrêté retourne un statut 503 et le
     * message « Le service ms-cible est momentanément indisponible. ».</p>
     *
     * @param ex      exception signalant le service injoignable
     * @param requete requête à l'origine de l'erreur
     * @return la réponse 503 normalisée
     */
    @ExceptionHandler(ServiceIndisponibleException.class)
    public ResponseEntity<Object> handleServiceIndisponible(
            ServiceIndisponibleException ex, HttpServletRequest requete) {
        return reponses.serveur(
                HttpStatus.SERVICE_UNAVAILABLE, List.of(ex.getMessage()), requete, ex);
    }

    /**
     * Traduit une erreur non anticipée en réponse 500, sans divulguer de détail technique.
     *
     * <p><b>Exemple :</b> une panne de base de données retourne un statut 500 et le seul
     * message « Une erreur interne est survenue. » ; la cause reste consultable dans les
     * journaux du service.</p>
     *
     * @param ex      exception inattendue remontée jusqu'au contrôleur
     * @param requete requête à l'origine de l'erreur
     * @return la réponse 500 normalisée
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAll(Exception ex, HttpServletRequest requete) {
        return reponses.serveur(
                HttpStatus.INTERNAL_SERVER_ERROR, List.of(MESSAGE_ERREUR_INTERNE), requete, ex);
    }

    /**
     * Traduit les erreurs de validation d'un corps de requête annoté {@code @Valid} en réponse 400.
     *
     * <p>Chaque champ refusé donne un message « champ: motif » ; les règles portant sur l'objet
     * entier donnent « objet: motif ».</p>
     *
     * <p><b>Exemple :</b> un patient envoyé sans nom retourne un statut 400 et le message
     * « lastName: Le nom de famille est obligatoire ».</p>
     *
     * @param ex         exception portant le résultat de la validation
     * @param enTetes    en-têtes prévus par Spring pour la réponse (non repris)
     * @param statut     statut prévu par Spring (non repris : toujours 400)
     * @param webRequest requête à l'origine de l'erreur
     * @return la réponse 400 normalisée
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders enTetes,
            HttpStatusCode statut, WebRequest webRequest) {

        List<String> messages = new ArrayList<>();
        for (FieldError erreurDeChamp : ex.getBindingResult().getFieldErrors()) {
            messages.add(erreurDeChamp.getField() + ": " + erreurDeChamp.getDefaultMessage());
        }
        ex.getBindingResult().getGlobalErrors()
                .forEach(err -> messages.add(err.getObjectName() + ": " + err.getDefaultMessage()));

        return reponses.client(HttpStatus.BAD_REQUEST, messages, requeteDe(webRequest));
    }

    /**
     * Traduit l'absence de route correspondant à l'URL appelée en réponse 404.
     *
     * <p><b>Exemple :</b> {@code GET /inconnue} retourne un statut 404 et le message
     * « Route non trouvée: GET /inconnue ».</p>
     *
     * @param ex         exception portant la méthode et l'URL demandées
     * @param enTetes    en-têtes prévus par Spring pour la réponse (non repris)
     * @param statut     statut prévu par Spring (non repris : toujours 404)
     * @param webRequest requête à l'origine de l'erreur
     * @return la réponse 404 normalisée
     */
    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(
            NoHandlerFoundException ex, HttpHeaders enTetes, HttpStatusCode statut,
            WebRequest webRequest) {

        String message = "Route non trouvée: " + ex.getHttpMethod() + " " + ex.getRequestURL();
        return reponses.client(HttpStatus.NOT_FOUND, List.of(message), requeteDe(webRequest));
    }

    // Extrait la requête servlet des handlers hérités, qui reçoivent un WebRequest.
    private HttpServletRequest requeteDe(WebRequest webRequest) {
        return (HttpServletRequest) webRequest.resolveReference(WebRequest.REFERENCE_REQUEST);
    }
}
