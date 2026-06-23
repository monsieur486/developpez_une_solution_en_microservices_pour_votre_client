package com.mr486.msrisque.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Note médicale d'un patient.
 *
 * <p><b>Exemple :</b> new Note("Le patient est fumeur") porte le contenu
 * analysé pour détecter les déclencheurs.</p>
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Note {
    private String content;
}
