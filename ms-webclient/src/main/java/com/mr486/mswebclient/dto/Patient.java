package com.mr486.mswebclient.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * Patient tel qu'exposé par ms-patients.
 *
 * <p><b>Exemple :</b> un patient d'identifiant 7, de genre "M", né le
 * 1990-01-01, avec ses coordonnées.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Patient {

    private Long id;
    private String firstName;
    private String lastName;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;
    private String gender;
    private String postalAddress;
    private String phoneNumber;
}
