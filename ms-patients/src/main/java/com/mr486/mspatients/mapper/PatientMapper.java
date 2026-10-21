package com.mr486.mspatients.mapper;

import com.mr486.commun.dto.PatientDto;
import com.mr486.commun.dto.PatientForm;
import com.mr486.mspatients.model.Patient;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Convertit les patients entre l'entité JPA et les objets échangés par l'API.
 *
 * <p>Seul point de passage entre les deux mondes : ajouter un champ au patient ne demande de
 * toucher qu'ici, pas au service ni au contrôleur.</p>
 *
 * <p><b>Exemple :</b> {@code versDto(patient)} produit le {@link PatientDto} renvoyé par
 * {@code GET /patients/{id}} ; {@code appliquer(form, patient)} recopie une saisie sur l'entité.</p>
 */
@Component
public class PatientMapper {

    /**
     * Construit la représentation API d'un patient.
     *
     * <p><b>Exemple :</b> le patient {@code TestNone} né le 1966-12-31 donne un DTO portant le
     * même identifiant, les mêmes nom, date de naissance, genre, adresse et téléphone.</p>
     *
     * @param patient entité lue en base
     * @return le DTO correspondant, champ pour champ
     */
    public PatientDto versDto(Patient patient) {
        return PatientDto.builder()
                .id(patient.getId())
                .firstName(patient.getFirstName())
                .lastName(patient.getLastName())
                .birthDate(patient.getBirthDate())
                .gender(patient.getGender())
                .postalAddress(patient.getPostalAddress())
                .phoneNumber(patient.getPhoneNumber())
                .build();
    }

    /**
     * Construit la représentation API d'une liste de patients, dans le même ordre.
     *
     * <p><b>Exemple :</b> une liste vide donne une liste vide ; quatre patients donnent quatre DTO.</p>
     *
     * @param patients entités lues en base
     * @return les DTO correspondants
     */
    public List<PatientDto> versListeDto(List<Patient> patients) {
        return patients.stream().map(this::versDto).toList();
    }

    /**
     * Recopie les données saisies sur l'entité, sans toucher à son identifiant.
     *
     * <p>Sert à la création (entité neuve) comme à la mise à jour (entité lue en base) : les
     * champs optionnels laissés vides effacent la valeur précédente.</p>
     *
     * <p><b>Exemple :</b> un formulaire avec l'adresse {@code "12 rue Neuve"} remplace l'adresse
     * du patient ; un formulaire sans téléphone met le téléphone à {@code null}.</p>
     *
     * @param patientForm données saisies, déjà validées
     * @param patient     entité à mettre à jour
     */
    public void appliquer(PatientForm patientForm, Patient patient) {
        patient.setFirstName(patientForm.getFirstName());
        patient.setLastName(patientForm.getLastName());
        patient.setBirthDate(patientForm.getBirthDate());
        patient.setGender(patientForm.getGender());
        patient.setPostalAddress(patientForm.getPostalAddress());
        patient.setPhoneNumber(patientForm.getPhoneNumber());
    }
}
