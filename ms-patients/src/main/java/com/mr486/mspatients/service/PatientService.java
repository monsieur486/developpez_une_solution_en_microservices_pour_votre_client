package com.mr486.mspatients.service;

import com.mr486.commun.dto.PatientForm;
import com.mr486.mspatients.exception.PatientDejaExistantException;
import com.mr486.mspatients.exception.PatientIntrouvableException;
import com.mr486.mspatients.mapper.PatientMapper;
import com.mr486.mspatients.model.Patient;
import com.mr486.mspatients.repository.PatientRepository;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Gère les fiches patients : consultation, création et mise à jour.
 *
 * <p>Porte la règle d'unicité : deux patients ne peuvent partager le même nom, prénom, date de
 * naissance et genre. Les journaux ne mentionnent que l'identifiant, jamais l'identité du
 * patient (données de santé).</p>
 *
 * <p><b>Exemple :</b> {@code patientService.findById(id)} renvoie la fiche, ou lève
 * {@link PatientIntrouvableException} que l'API traduit en 404.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PatientService {
    private final PatientRepository patientRepository;
    private final PatientMapper patientMapper;

    /**
     * Liste tous les patients enregistrés.
     *
     * <p><b>Exemple :</b> avec le jeu de démonstration chargé par Liquibase, renvoie les quatre
     * patients {@code TestNone}, {@code TestBorderline}, {@code TestInDanger} et {@code TestEarlyOnset}.</p>
     *
     * @return les patients, liste vide s'il n'y en a aucun
     */
    public List<Patient> findAll() {
        return patientRepository.findAll();
    }

    /**
     * Récupère un patient par son identifiant.
     *
     * <p><b>Exemple :</b> un identifiant existant renvoie la fiche ; un identifiant inconnu lève
     * {@link PatientIntrouvableException} avec le message « Aucun patient avec l'id: … ».</p>
     *
     * @param id identifiant du patient
     * @return le patient correspondant
     * @throws PatientIntrouvableException si aucun patient ne porte cet identifiant
     */
    public Patient findById(Long id) {
        return patientRepository
                .findById(id)
                .orElseThrow(() -> new PatientIntrouvableException(id));
    }

    /**
     * Crée un patient après contrôle d'unicité de son identité.
     *
     * <p><b>Exemple :</b> un nouveau patient est enregistré et reçoit un identifiant ; un patient
     * déjà présent (même nom, prénom, date de naissance et genre) est refusé.</p>
     *
     * @param patientForm données saisies, déjà validées
     * @return le patient enregistré, avec son identifiant
     * @throws PatientDejaExistantException si un patient de même identité existe déjà
     */
    @Transactional
    public Patient savePatient(PatientForm patientForm) {
        verifieAbsenceDeDoublon(patientForm);

        Patient patient = new Patient();
        patientMapper.appliquer(patientForm, patient);
        Patient enregistre = patientRepository.save(patient);

        log.info("patient {} créé", enregistre.getId());
        return enregistre;
    }

    /**
     * Met à jour la fiche d'un patient existant.
     *
     * <p>Le contrôle d'unicité n'est fait que si l'identité change : corriger une adresse ou un
     * téléphone reste toujours possible.</p>
     *
     * <p><b>Exemple :</b> changer l'adresse d'un patient réussit ; renommer un patient vers
     * l'identité d'un autre est refusé.</p>
     *
     * @param id          identifiant du patient à modifier
     * @param patientForm nouvelles données, déjà validées
     * @return le patient mis à jour
     * @throws PatientIntrouvableException  si aucun patient ne porte cet identifiant
     * @throws PatientDejaExistantException si la nouvelle identité est déjà prise
     */
    @Transactional
    public Patient updatePatient(Long id, PatientForm patientForm) {
        Patient existant = findById(id);

        if (identiteModifiee(existant, patientForm)) {
            verifieAbsenceDeDoublon(patientForm);
        }
        patientMapper.appliquer(patientForm, existant);
        Patient miseAJour = patientRepository.save(existant);

        log.info("patient {} mis à jour", id);
        return miseAJour;
    }

    // Lève PatientDejaExistantException si l'identité saisie appartient déjà à un patient.
    private void verifieAbsenceDeDoublon(PatientForm patientForm) {
        boolean existeDeja = patientRepository.existsByLastNameAndFirstNameAndBirthDateAndGender(
                patientForm.getLastName(),
                patientForm.getFirstName(),
                patientForm.getBirthDate(),
                patientForm.getGender());

        log.debug("contrôle d'unicité effectué, doublon détecté : {}", existeDeja);
        if (existeDeja) {
            throw new PatientDejaExistantException();
        }
    }

    // Indique si le formulaire change l'un des champs qui composent l'identité du patient.
    private boolean identiteModifiee(Patient existant, PatientForm patientForm) {
        return !Objects.equals(existant.getLastName(), patientForm.getLastName())
                || !Objects.equals(existant.getFirstName(), patientForm.getFirstName())
                || !Objects.equals(existant.getBirthDate(), patientForm.getBirthDate())
                || !Objects.equals(existant.getGender(), patientForm.getGender());
    }
}
