package com.mr486.mswebclient.service;

import com.mr486.commun.dto.PatientDto;
import com.mr486.commun.dto.PatientForm;
import com.mr486.mswebclient.configuration.ConstantesWebclient;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

/**
 * Appels à l'API de {@code ms-patients}, à travers la gateway.
 *
 * <p><b>Exemple :</b> {@code patientApiService.recupereLePatient(1L)} interroge
 * {@code GET /ms-patients/patients/1}.</p>
 */
@Service
@RequiredArgsConstructor
public class PatientApiService {
    private final ClientPasserelle clientPasserelle;

    /**
     * Liste tous les patients.
     *
     * <p><b>Exemple :</b> avec le jeu de démonstration, renvoie quatre patients.</p>
     *
     * @return les patients, liste vide s'il n'y en a aucun
     */
    public List<PatientDto> listeLesPatients() {
        return clientPasserelle.echange(
                HttpMethod.GET,
                ConstantesWebclient.CHEMIN_PATIENTS,
                null,
                new ParameterizedTypeReference<List<PatientDto>>() {},
                ConstantesWebclient.SERVICE_PATIENTS);
    }

    /**
     * Récupère la fiche d'un patient.
     *
     * <p><b>Exemple :</b> un identifiant inconnu lève {@code ResourceNotFoundException}.</p>
     *
     * @param id identifiant du patient
     * @return la fiche du patient
     */
    public PatientDto recupereLePatient(Long id) {
        return clientPasserelle.echange(
                HttpMethod.GET,
                ConstantesWebclient.CHEMIN_PATIENTS + "/" + id,
                null,
                new ParameterizedTypeReference<PatientDto>() {},
                ConstantesWebclient.SERVICE_PATIENTS);
    }

    /**
     * Crée un patient.
     *
     * <p><b>Exemple :</b> un patient déjà enregistré lève {@code DuplicateException}.</p>
     *
     * @param formulaire données du patient, déjà validées
     * @return la fiche créée, avec son identifiant
     */
    public PatientDto creeLePatient(PatientForm formulaire) {
        return clientPasserelle.echange(
                HttpMethod.POST,
                ConstantesWebclient.CHEMIN_PATIENTS,
                formulaire,
                new ParameterizedTypeReference<PatientDto>() {},
                ConstantesWebclient.SERVICE_PATIENTS);
    }

    /**
     * Met à jour la fiche d'un patient.
     *
     * <p><b>Exemple :</b> renommer un patient vers l'identité d'un autre lève {@code DuplicateException}.</p>
     *
     * @param id         identifiant du patient
     * @param formulaire nouvelles données, déjà validées
     * @return la fiche mise à jour
     */
    public PatientDto metAJourLePatient(Long id, PatientForm formulaire) {
        return clientPasserelle.echange(
                HttpMethod.PUT,
                ConstantesWebclient.CHEMIN_PATIENTS + "/" + id,
                formulaire,
                new ParameterizedTypeReference<PatientDto>() {},
                ConstantesWebclient.SERVICE_PATIENTS);
    }
}
