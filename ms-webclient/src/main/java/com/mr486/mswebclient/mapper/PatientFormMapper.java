package com.mr486.mswebclient.mapper;

import com.mr486.commun.dto.PatientDto;
import com.mr486.commun.dto.PatientForm;
import org.springframework.stereotype.Component;

/**
 * Prépare le formulaire de modification à partir de la fiche renvoyée par l'API.
 *
 * <p>L'identifiant n'est pas recopié : il voyage dans l'URL de la page, pas dans le formulaire.</p>
 *
 * <p><b>Exemple :</b> la fiche du patient {@code TestNone} donne un formulaire pré-rempli avec
 * ses nom, prénom, date de naissance, genre, adresse et téléphone.</p>
 */
@Component
public class PatientFormMapper {

    /**
     * Construit le formulaire pré-rempli d'un patient existant.
     *
     * <p><b>Exemple :</b> une fiche sans téléphone donne un formulaire dont le téléphone vaut
     * {@code null}, affiché comme un champ vide.</p>
     *
     * @param patient fiche lue via la gateway
     * @return le formulaire correspondant, champ pour champ
     */
    public PatientForm versFormulaire(PatientDto patient) {
        return PatientForm.builder()
                .firstName(patient.getFirstName())
                .lastName(patient.getLastName())
                .birthDate(patient.getBirthDate())
                .gender(patient.getGender())
                .postalAddress(patient.getPostalAddress())
                .phoneNumber(patient.getPhoneNumber())
                .build();
    }
}
