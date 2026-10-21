package com.mr486.msrisque.configuration;

import java.util.List;

/**
 * Constantes de l'évaluation du risque : termes déclencheurs, seuils et noms de services.
 *
 * <p>Les seuils viennent du cahier des charges. Un seuil est le nombre minimal de termes distincts
 * pour atteindre le niveau.</p>
 *
 * <p><b>Exemple :</b> une femme de moins de 30 ans passe {@code In Danger} à partir de
 * {@link #SEUIL_IN_DANGER_FEMME_JEUNE} termes.</p>
 */
public final class ConstantesRisque {
    /**
     * Termes déclencheurs, chacun sous toutes ses graphies en minuscules : un terme compte une
     * seule fois, quelle que soit la graphie relevée et le nombre de notes qui le citent.
     *
     * <p>La recherche se fait par sous-chaîne : « vertige » couvre aussi « Vertiges », le terme
     * officiel, et « anormal » couvre « anormale » ou « anormales ».</p>
     *
     * <p><b>Exemple :</b> « fumeur » et « fumeuse » relevés dans deux notes comptent pour un.</p>
     */
    public static final List<List<String>> TERMES_DECLENCHEURS = List.of(
            List.of("hémoglobine a1c"),
            List.of("microalbumine"),
            List.of("taille"),
            List.of("poids"),
            List.of("fumeur", "fumeuse"),
            List.of("anormal"),
            List.of("cholestérol"),
            List.of("vertige"),
            List.of("rechute"),
            List.of("réaction"),
            List.of("anticorps"));

    /** Genre masculin tel que l'enregistre {@code ms-patients} ; tout autre genre suit les règles des femmes. */
    public static final String GENRE_MASCULIN = "M";

    /** Nom du service des patients, repris dans les messages d'erreur. */
    public static final String SERVICE_PATIENTS = "ms-patients";

    /** Nom du service des notes, repris dans les messages d'erreur. */
    public static final String SERVICE_NOTES = "ms-notes";

    /** Âge à partir duquel s'appliquent les règles des 30 ans et plus (30 ans pile compris). */
    public static final int AGE_PIVOT = 30;

    /** 30 ans et plus : minimum pour {@code Borderline}. */
    public static final int SEUIL_BORDERLINE = 2;

    /** 30 ans et plus : minimum pour {@code In Danger}. */
    public static final int SEUIL_IN_DANGER_TRENTE_ET_PLUS = 6;

    /** 30 ans et plus : minimum pour {@code Early onset}. */
    public static final int SEUIL_EARLY_ONSET_TRENTE_ET_PLUS = 8;

    /** Homme de moins de 30 ans : minimum pour {@code In Danger}. */
    public static final int SEUIL_IN_DANGER_HOMME_JEUNE = 3;

    /** Homme de moins de 30 ans : minimum pour {@code Early onset}. */
    public static final int SEUIL_EARLY_ONSET_HOMME_JEUNE = 5;

    /** Femme de moins de 30 ans : minimum pour {@code In Danger}. */
    public static final int SEUIL_IN_DANGER_FEMME_JEUNE = 4;

    /** Femme de moins de 30 ans : minimum pour {@code Early onset}. */
    public static final int SEUIL_EARLY_ONSET_FEMME_JEUNE = 7;

    private ConstantesRisque() {
    }
}
