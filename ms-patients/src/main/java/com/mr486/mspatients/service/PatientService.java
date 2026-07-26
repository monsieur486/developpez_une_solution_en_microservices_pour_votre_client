package com.mr486.mspatients.service;

import com.mr486.mspatients.configuration.ConstantesApplication;
import com.mr486.mspatients.dto.PatientForm;
import com.mr486.mspatients.exception.NotFoundException;
import com.mr486.mspatients.model.Patient;
import com.mr486.mspatients.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

/**
 * Gère les patients : lecture, création et mise à jour.
 *
 * <p><b>Exemple :</b> findById(7L) retourne le patient d'id 7 ou lève une
 * NotFoundException ; savePatient(form) crée un patient à partir du formulaire.</p>
 */
@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;

    /**
     * Retourne une page de patients, triés par identifiant.
     *
     * <p><b>Exemple :</b> findAll(0) retourne les 20 premiers patients ; avec 45
     * patients en base, la page porte totalPages=3.</p>
     *
     * @param page numéro de la page demandée (à partir de 0)
     * @return la page de patients correspondante
     */
    public Page<Patient> findAll(int page) {
        return patientRepository.findAll(
                PageRequest.of(page, ConstantesApplication.TAILLE_PAGE_PATIENTS, Sort.by("id")));
    }

    /**
     * Retourne un patient par son identifiant.
     *
     * <p><b>Exemple :</b> findById(7L) retourne le patient 7 ; un id inexistant
     * lève NotFoundException.</p>
     *
     * @param id identifiant du patient
     * @return le patient correspondant
     * @throws NotFoundException si aucun patient ne correspond
     */
    public Patient findById(Long id) {
        return patientRepository
                .findById(id)
                .orElseThrow(() -> new NotFoundException("aucun patient trouvé avec l'id: " + id));
    }

    /**
     * Crée un patient à partir d'un formulaire.
     *
     * <p><b>Exemple :</b> savePatient(form) persiste un nouveau patient et
     * retourne l'entité enregistrée (avec son id généré).</p>
     *
     * @param patientForm les données du patient à créer
     * @return le patient créé
     */
    public Patient savePatient(PatientForm patientForm) {
        Patient patient = new Patient();
        remplitDepuisFormulaire(patient, patientForm);
        return patientRepository.save(patient);
    }

    /**
     * Met à jour un patient existant à partir d'un formulaire.
     *
     * <p><b>Exemple :</b> updatePatient(7L, form) met à jour le patient 7 ; un id
     * inexistant lève NotFoundException.</p>
     *
     * @param id          identifiant du patient à mettre à jour
     * @param patientForm les nouvelles données du patient
     * @return le patient mis à jour
     * @throws NotFoundException si aucun patient ne correspond
     */
    public Patient updatePatient(Long id, PatientForm patientForm) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("aucun patient trouvé avec l'id: " + id));
        remplitDepuisFormulaire(patient, patientForm);
        return patientRepository.save(patient);
    }

    // Recopie les champs du formulaire dans l'entité patient.
    private void remplitDepuisFormulaire(Patient patient, PatientForm patientForm) {
        patient.setFirstName(patientForm.getFirstName());
        patient.setLastName(patientForm.getLastName());
        patient.setBirthDate(patientForm.getBirthDate());
        patient.setGender(patientForm.getGender());
        patient.setPostalAddress(patientForm.getPostalAddress());
        patient.setPhoneNumber(patientForm.getPhoneNumber());
    }
}
