package com.mr486.mspatients.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

/**
 * Formulaire de création/mise à jour d'un patient (données validées en entrée).
 *
 * <p><b>Exemple :</b> un formulaire avec prénom, nom, date de naissance et genre
 * renseignés est valide ; un champ obligatoire vide déclenche une erreur 400.</p>
 */
@Data
@Builder
public class PatientForm {

    @NotBlank(message = "le prénom ne peut pas être vide")
    private String firstName;

    @NotBlank(message = "le nom de famille ne peut pas être vide")
    private String lastName;

    @NotNull(message = "la date de naissance ne peut pas être nulle")
    private LocalDate birthDate;

    @NotBlank(message = "le genre ne peut pas être vide")
    private String gender;

    private String postalAddress;
    private String phoneNumber;
}
