package com.mr486.mswebclient.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Note médicale d'un patient.
 *
 * <p><b>Exemple :</b> new Note("Le patient est fumeur") porte le contenu saisi
 * dans le formulaire d'ajout.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Note {

    private String content;
}
