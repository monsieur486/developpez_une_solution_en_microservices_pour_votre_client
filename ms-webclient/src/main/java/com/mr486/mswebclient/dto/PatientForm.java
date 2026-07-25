package com.mr486.mswebclient.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * Formulaire de création ou de modification d'un patient (sans identifiant :
 * celui-ci vient de l'URL).
 *
 * <p><b>Exemple :</b> le formulaire d'ajout envoie prénom, nom, date de
 * naissance, genre et coordonnées à ms-patients.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientForm {

    private String firstName;
    private String lastName;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;
    private String gender;
    private String postalAddress;
    private String phoneNumber;
}
