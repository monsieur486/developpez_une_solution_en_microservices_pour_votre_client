package com.mr486.commun.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Note saisie par un praticien, avant son enregistrement.
 *
 * <p>Le formulaire ne porte que le texte : le patient est désigné par l'URL et la date est
 * attribuée par {@code ms-notes} à l'enregistrement. Le texte est conservé tel quel, casse
 * comprise, car l'évaluation du risque y recherche des termes déclencheurs.</p>
 *
 * <p><b>Exemple :</b> {@code NoteForm.builder().content("Le patient déclare qu'il fume depuis peu")
 * .build()} est un formulaire valide ; un texte vide ou fait d'espaces est refusé.</p>
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NoteForm {

    // Une note de consultation tient en quelques lignes : la borne empêche seulement d'envoyer
    // un texte démesuré, qui serait stocké puis renvoyé à chaque affichage de la fiche.
    private static final int LONGUEUR_MAX = 5000;

    /** Texte libre de la note (obligatoire, 5000 caractères au plus). */
    @NotBlank(message = "Le contenu de la note est obligatoire")
    @Size(max = LONGUEUR_MAX, message = "La note ne doit pas dépasser 5000 caractères")
    private String content;
}
