package com.mr486.msrisque.service;

import com.mr486.msrisque.dto.Note;
import com.mr486.msrisque.dto.Patient;
import com.mr486.msrisque.dto.Risque;
import com.mr486.msrisque.model.Genre;
import com.mr486.msrisque.model.NiveauRisque;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;

/**
 * Évalue le niveau de risque diabétique d'un patient à partir de ses données
 * démographiques et des termes déclencheurs présents dans ses notes médicales.
 *
 * <p><b>Exemple :</b> evalueRisque(2L) retourne un Risque de niveau
 * {@code "Borderline"} pour le patient TestBorderline du jeu de démonstration
 * (plus de 30 ans, 2 à 5 déclencheurs) — jeu décrit dans le README racine.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EvaluationService {

    /** Âge pivot séparant les patients jeunes des patients plus âgés. */
    private static final int AGE_PIVOT = 30;

    /** Termes déclencheurs recherchés dans les notes médicales (en minuscules). */
    private static final List<String> TERMES_DECLENCHEURS = List.of(
            "hémoglobine a1c",
            "microalbumine",
            "taille",
            "poids",
            "fumeur",
            "anormal",
            "cholestérol",
            "vertige",
            "rechute",
            "réaction",
            "anticorps"
    );

    private final PatientService patientService;
    private final NotesService notesService;
    private final Clock clock;

    /**
     * Évalue le niveau de risque diabétique d'un patient.
     *
     * <p><b>Cas d'usage</b> (jeu de démonstration, décrit dans le README racine) :</p>
     *
     * <ul>
     *   <li>aucun déclencheur → {@code "None"} (patient 1, TestNone) ;</li>
     *   <li>2 à 5 déclencheurs après 30 ans → {@code "Borderline"} (patient 2,
     *       TestBorderline) ;</li>
     *   <li>3 déclencheurs pour un homme de moins de 30 ans → {@code "In Danger"}
     *       (patient 3, TestInDanger) ;</li>
     *   <li>7 déclencheurs ou plus pour une femme de moins de 30 ans →
     *       {@code "Early onset"} (patient 4, TestEarlyOnset).</li>
     * </ul>
     *
     * <p><b>Exemple :</b> evalueRisque(3L) retourne un Risque de niveau
     * {@code "In Danger"} pour le patient TestInDanger du jeu de démonstration.</p>
     *
     * @param patientId identifiant du patient à évaluer
     * @return le risque calculé pour ce patient
     */
    public Risque evalueRisque(Long patientId) {
        Patient patient = patientService.getPatientById(patientId);
        List<Note> notes = notesService.getNotesByPatientId(patientId);

        String genre = patient.getGender();
        int age = calculAge(patient.getBirthDate());
        int declencheursCount = compteDeclencheurs(notes);
        log.debug("Patient {} : âge={}, déclencheurs={}", patientId, age, declencheursCount);

        NiveauRisque niveau = determineNiveau(age, genre, declencheursCount);
        log.debug("Patient {} : niveau de risque={}", patientId, niveau);

        return new Risque(niveau.getLibelle());
    }

    // Détermine le niveau de risque à partir de l'âge, du genre et du nombre de déclencheurs.
    private NiveauRisque determineNiveau(int age, String genre, int declencheursCount) {
        boolean homme = Genre.HOMME.correspond(genre);
        boolean femme = Genre.FEMME.correspond(genre);
        boolean jeune = age < AGE_PIVOT;
        boolean ageEleve = age > AGE_PIVOT;

        if (declencheursCount == 0) {
            return NiveauRisque.AUCUN;
        }
        if (estPrecoce(jeune, ageEleve, homme, femme, declencheursCount)) {
            return NiveauRisque.PRECOCE;
        }
        if (estEnDanger(jeune, ageEleve, homme, femme, declencheursCount)) {
            return NiveauRisque.DANGER;
        }
        if (declencheursCount >= 2 && declencheursCount <= 5 && ageEleve) {
            return NiveauRisque.LIMITE;
        }
        return NiveauRisque.AUCUN;
    }

    // Vérifie les seuils d'apparition précoce ("Early onset").
    private boolean estPrecoce(boolean jeune, boolean ageEleve, boolean homme,
                               boolean femme, int declencheursCount) {
        return (jeune && homme && declencheursCount >= 5)
                || (jeune && femme && declencheursCount >= 7)
                || (ageEleve && declencheursCount >= 8);
    }

    // Vérifie les seuils de mise en danger ("In Danger").
    private boolean estEnDanger(boolean jeune, boolean ageEleve, boolean homme,
                                boolean femme, int declencheursCount) {
        return (jeune && homme && declencheursCount == 3)
                || (jeune && femme && declencheursCount == 4)
                || (ageEleve && (declencheursCount == 6 || declencheursCount == 7));
    }

    // Compte le nombre de termes déclencheurs présents dans l'ensemble des notes.
    private int compteDeclencheurs(List<Note> notes) {
        int declencheursCount = 0;
        for (Note note : notes) {
            String contenu = note.getContent();
            if (contenu == null) {
                continue;
            }
            contenu = contenu.toLowerCase();
            for (String terme : TERMES_DECLENCHEURS) {
                if (contenu.contains(terme)) {
                    declencheursCount++;
                }
            }
        }
        return declencheursCount;
    }

    // Calcule l'âge révolu du patient à la date du jour (horloge injectée).
    private int calculAge(LocalDate birthDate) {
        return Period.between(birthDate, LocalDate.now(clock)).getYears();
    }
}
