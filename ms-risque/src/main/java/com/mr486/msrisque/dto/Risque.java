package com.mr486.msrisque.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Risque de diabète calculé pour un patient.
 *
 * <p><b>Exemple :</b> new Risque("Borderline") représente un risque limite.</p>
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Risque {
    private String level;
}
