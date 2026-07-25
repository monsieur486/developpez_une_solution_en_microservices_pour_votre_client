package com.mr486.mswebclient.service;

import com.mr486.mswebclient.dto.Patient;
import com.mr486.mswebclient.dto.PatientForm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * Accède aux patients exposés par ms-patients au travers de la passerelle.
 *
 * <p><b>Exemple :</b> getPatients() retourne la liste des patients ;
 * createPatient(form) crée un nouveau patient.</p>
 */
@Service
public class PatientService {

    private final RestTemplate restTemplate;
    private final String gatewayBase;

    /**
     * Construit le service avec le client REST et l'URL de la passerelle.
     *
     * <p><b>Exemple :</b> new PatientService(restTemplate, "http://localhost:9000")
     * appellera http://localhost:9000/ms-patients/patients.</p>
     *
     * @param restTemplate le client REST authentifié vers la passerelle
     * @param gatewayBase  l'URL de base de la passerelle
     */
    public PatientService(RestTemplate restTemplate,
                          @Value("${app.gateway.base-url}") String gatewayBase) {
        this.restTemplate = restTemplate;
        this.gatewayBase = gatewayBase;
    }

    /**
     * Retourne la liste de tous les patients.
     *
     * <p><b>Exemple :</b> getPatients() retourne les patients enregistrés (liste
     * vide si aucun).</p>
     *
     * @return la liste des patients
     */
    public List<Patient> getPatients() {
        ResponseEntity<List<Patient>> response = restTemplate.exchange(
                gatewayBase + "/ms-patients/patients",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );
        return response.getBody();
    }

    /**
     * Retourne un patient par son identifiant.
     *
     * <p><b>Exemple :</b> getPatientById(7L) retourne le patient d'identifiant 7.</p>
     *
     * @param id identifiant du patient
     * @return le patient correspondant
     */
    public Patient getPatientById(Long id) {
        ResponseEntity<Patient> response = restTemplate.exchange(
                gatewayBase + "/ms-patients/patients/" + id,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );
        return response.getBody();
    }

    /**
     * Crée un nouveau patient.
     *
     * <p><b>Exemple :</b> createPatient(form) envoie le formulaire à ms-patients
     * qui persiste le patient.</p>
     *
     * @param patient le formulaire du patient à créer
     */
    public void createPatient(PatientForm patient) {
        restTemplate.exchange(
                gatewayBase + "/ms-patients/patients",
                HttpMethod.POST,
                new HttpEntity<>(patient),
                new ParameterizedTypeReference<>() {
                }
        );
    }

    /**
     * Met à jour un patient existant.
     *
     * <p><b>Exemple :</b> updatePatient(7L, form) remplace les données du patient
     * 7 par celles du formulaire.</p>
     *
     * @param id      identifiant du patient à modifier
     * @param patient le formulaire portant les nouvelles données
     */
    public void updatePatient(Long id, PatientForm patient) {
        restTemplate.exchange(
                gatewayBase + "/ms-patients/patients/" + id,
                HttpMethod.PUT,
                new HttpEntity<>(patient),
                new ParameterizedTypeReference<>() {
                }
        );
    }
}
