package com.mr486.msrisque.model;

/**
 * Niveau de risque diabétique d'un patient.
 *
 * <p>Chaque valeur porte son libellé métier (en anglais, tel qu'attendu par l'API)
 * afin d'éviter des chaînes en dur dispersées dans le code.</p>
 *
 * <p><b>Exemple :</b> NiveauRisque.DANGER.getLibelle() retourne "In Danger".</p>
 */
public enum NiveauRisque {

    /** Aucun déclencheur détecté. */
    AUCUN("None"),

    /** Risque limite (« borderline »). */
    LIMITE("Borderline"),

    /** Patient en danger. */
    DANGER("In Danger"),

    /** Apparition précoce. */
    PRECOCE("Early onset");

    private final String libelle;

    NiveauRisque(String libelle) {
        this.libelle = libelle;
    }

    /**
     * Retourne le libellé métier du niveau de risque.
     *
     * <p><b>Exemple :</b> NiveauRisque.LIMITE.getLibelle() retourne "Borderline".</p>
     *
     * @return le libellé tel qu'exposé par l'API
     */
    public String getLibelle() {
        return libelle;
    }
}
