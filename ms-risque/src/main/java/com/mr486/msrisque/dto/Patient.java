package com.mr486.msrisque.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * Données démographiques d'un patient.
 *
 * <p><b>Exemple :</b> un patient de genre "M" né le 1990-01-01 sert de base au
 * calcul de l'âge et du risque.</p>
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
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
