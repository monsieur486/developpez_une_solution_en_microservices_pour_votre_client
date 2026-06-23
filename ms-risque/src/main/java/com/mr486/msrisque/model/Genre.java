package com.mr486.msrisque.model;

/**
 * Genre d'un patient, tel que transmis par le microservice ms-patients.
 *
 * <p><b>Exemple :</b> Genre.HOMME.correspond("m") retourne true (insensible à
 * la casse).</p>
 */
public enum Genre {

    /** Homme (code "M"). */
    HOMME("M"),

    /** Femme (code "F"). */
    FEMME("F");

    private final String code;

    Genre(String code) {
        this.code = code;
    }

    /**
     * Indique si un code de genre correspond à cette valeur (insensible à la casse).
     *
     * <p><b>Exemple :</b> Genre.FEMME.correspond("F") retourne true ;
     * correspond(null) retourne false.</p>
     *
     * @param code le code de genre à comparer (peut être null)
     * @return true si le code correspond à cette valeur
     */
    public boolean correspond(String code) {
        return this.code.equalsIgnoreCase(code);
    }
}
