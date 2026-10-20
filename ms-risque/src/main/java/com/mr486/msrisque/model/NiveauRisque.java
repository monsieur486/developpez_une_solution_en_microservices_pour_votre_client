package com.mr486.msrisque.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Niveaux de risque de diabète définis par le cahier des charges, du plus faible au plus élevé.
 *
 * <p>Le libellé est la valeur renvoyée par l'API, en texte brut. Il reprend l'écriture du résumé
 * des besoins fonctionnels, espaces compris : {@code In Danger} et {@code Early onset}.</p>
 *
 * <p><b>Exemple :</b> {@code NiveauRisque.IN_DANGER.getLibelle()} retourne {@code "In Danger"}.</p>
 */
@Getter
@RequiredArgsConstructor
public enum NiveauRisque {
    /** Aucun risque : le dossier ne contient aucun terme déclencheur. */
    NONE("None"),

    /** Risque limité. */
    BORDERLINE("Borderline"),

    /** Danger. */
    IN_DANGER("In Danger"),

    /** Apparition précoce. */
    EARLY_ONSET("Early onset");

    /** Libellé renvoyé par l'API et affiché sur la fiche du patient. */
    private final String libelle;
}
