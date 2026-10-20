package com.mr486.commun.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * Données saisies pour créer ou mettre à jour la fiche d'un patient.
 *
 * <p>Le formulaire ne porte pas d'identifiant : à la création, il est attribué par la base,
 * et à la mise à jour, il est fourni dans l'URL. L'adresse postale et le numéro de téléphone
 * sont optionnels, comme le prévoit le besoin fonctionnel.</p>
 *
 * <p><b>Exemple :</b> {@code PatientForm.builder().firstName("Test").lastName("TestNone")
 * .birthDate(LocalDate.of(1966, 12, 31)).gender("F").build()} est un formulaire valide.</p>
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PatientForm {

    // Aligné sur les colonnes VARCHAR(255) de la table patients : au-delà, la base refuserait.
    private static final int LONGUEUR_MAX = 255;

    /** Prénom du patient (obligatoire). */
    @NotBlank(message = "Le prénom est obligatoire")
    @Size(max = LONGUEUR_MAX, message = "Le prénom ne doit pas dépasser 255 caractères")
    private String firstName;

    /** Nom de famille du patient (obligatoire). */
    @NotBlank(message = "Le nom de famille est obligatoire")
    @Size(max = LONGUEUR_MAX, message = "Le nom de famille ne doit pas dépasser 255 caractères")
    private String lastName;

    /** Date de naissance, au format {@code yyyy-MM-dd}, nécessairement passée. */
    @NotNull(message = "La date de naissance est obligatoire")
    @Past(message = "La date de naissance doit être dans le passé")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    /** Genre du patient : {@code M} ou {@code F}, utilisé plus tard pour évaluer le risque de diabète. */
    @NotBlank(message = "Le genre est obligatoire")
    @Pattern(regexp = "[MF]", message = "Le genre doit valoir M ou F")
    private String gender;

    /** Adresse postale (optionnelle). */
    @Size(max = LONGUEUR_MAX, message = "L'adresse ne doit pas dépasser 255 caractères")
    private String postalAddress;

    /** Numéro de téléphone (optionnel). */
    @Size(max = LONGUEUR_MAX, message = "Le téléphone ne doit pas dépasser 255 caractères")
    private String phoneNumber;
}
