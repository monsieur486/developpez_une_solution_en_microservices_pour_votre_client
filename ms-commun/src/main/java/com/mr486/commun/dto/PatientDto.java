package com.mr486.commun.dto;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * Fiche d'un patient telle que l'API la restitue.
 *
 * <p>Partagée entre {@code ms-patients}, qui la produit, et {@code ms-webclient}, qui l'affiche :
 * l'entité JPA ne quitte jamais le microservice des patients.</p>
 *
 * <p><b>Exemple :</b> {@code GET /patients/1} renvoie
 * {@code {"id":1,"firstName":"Test","lastName":"TestNone","birthDate":"1966-12-31","gender":"F",
 * "postalAddress":"1 Brookside St","phoneNumber":"100-222-3333"}}.</p>
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PatientDto {

    /** Identifiant technique attribué par la base. */
    private Long id;

    /** Prénom du patient. */
    private String firstName;

    /** Nom de famille du patient. */
    private String lastName;

    /** Date de naissance, au format {@code yyyy-MM-dd}. */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    /** Genre du patient : {@code M} ou {@code F}. */
    private String gender;

    /** Adresse postale, {@code null} si non renseignée. */
    private String postalAddress;

    /** Numéro de téléphone, {@code null} si non renseigné. */
    private String phoneNumber;
}
