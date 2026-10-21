package com.mr486.msrisque.service;

import com.mr486.commun.dto.NoteDto;
import com.mr486.commun.dto.PatientDto;
import com.mr486.msrisque.configuration.ConstantesRisque;
import com.mr486.msrisque.model.NiveauRisque;
import java.time.Clock;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Évalue le risque de diabète d'un patient à partir de sa fiche et des notes du praticien.
 *
 * <p>Le service ne stocke rien : il récupère l'âge et le genre auprès de {@code ms-patients}, les
 * notes auprès de {@code ms-notes}, puis applique les règles du cahier des charges. Chaque terme
 * déclencheur compte une seule fois, quelle que soit sa casse, sa graphie et le nombre de notes
 * qui le citent. L'âge se calcule avec l'horloge injectée, ce qui rend les tests déterministes.</p>
 *
 * <p><b>Exemple :</b> le patient de démonstration {@code TestEarlyOnset}, une femme de 24 ans dont
 * les notes citent sept termes distincts, est évalué {@code Early onset}.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EvaluationService {
    private final PatientService patientService;
    private final NotesService notesService;
    private final Clock horloge;

    /**
     * Calcule le niveau de risque de diabète d'un patient.
     *
     * <p><b>Exemple :</b> {@code evaluationDuRisque(TEST_BORDERLINE)} retourne {@code "Borderline"} pour
     * un homme de 81 ans dont les notes citent « anormale » et « réaction » ; un patient inconnu
     * lève {@code PatientIntrouvableException}.</p>
     *
     * <ul>
     *   <li>aucun terme déclencheur → {@code None} ;</li>
     *   <li>30 ans ou plus, 2 à 5 termes → {@code Borderline} ;</li>
     *   <li>30 ans ou plus, 6 ou 7 termes ; homme de moins de 30 ans, 3 ou 4 ; femme de moins de
     *       30 ans, 4 à 6 → {@code In Danger} ;</li>
     *   <li>30 ans ou plus, 8 termes ou plus ; homme de moins de 30 ans, 5 ou plus ; femme de moins
     *       de 30 ans, 7 ou plus → {@code Early onset} ;</li>
     *   <li>tout autre cas (un seul terme après 30 ans, trop peu de termes avant) → {@code None}.</li>
     * </ul>
     *
     * @param patientId identifiant du patient dans {@code ms-patients}
     * @return le libellé du niveau : {@code None}, {@code Borderline}, {@code In Danger} ou
     *     {@code Early onset}
     * @throws com.mr486.msrisque.exception.PatientIntrouvableException si le patient n'existe pas
     * @throws com.mr486.commun.exception.ServiceIndisponibleException si {@code ms-patients} ou
     *     {@code ms-notes} est injoignable
     * @throws com.mr486.commun.exception.AppelRefuseException si l'un d'eux refuse l'appel (4xx)
     */
    public String evaluationDuRisque(Long patientId) {
        PatientDto patient = patientService.getPatientById(patientId);
        List<NoteDto> notes = notesService.getNotesByPatientId(patientId);

        int age = calculeAge(patient.getBirthDate());
        int nombreDeDeclencheurs = compteDeclencheurs(notes);
        NiveauRisque niveauRisque = determineNiveau(age, patient.getGender(), nombreDeDeclencheurs);

        log.info("évaluation du risque calculée pour le patient {}", patientId);
        return niveauRisque.getLibelle();
    }

    // Compte les termes distincts présents dans l'ensemble des notes, sous l'une de leurs graphies.
    private int compteDeclencheurs(List<NoteDto> notes) {
        String dossier = notes.stream()
                .map(NoteDto::getContent)
                .filter(Objects::nonNull)
                .map(contenu -> contenu.toLowerCase(Locale.ROOT))
                .collect(Collectors.joining("\n"));
        int nombreDeDeclencheurs = (int) ConstantesRisque.TERMES_DECLENCHEURS.stream()
                .filter(graphies -> graphies.stream().anyMatch(dossier::contains))
                .count();
        log.debug("{} déclencheur(s) relevé(s) dans {} note(s)", nombreDeDeclencheurs, notes.size());
        return nombreDeDeclencheurs;
    }

    // Âge en années révolues à la date de l'horloge injectée.
    private int calculeAge(LocalDate dateDeNaissance) {
        return Period.between(dateDeNaissance, LocalDate.now(horloge)).getYears();
    }

    // Aiguille vers les règles des moins de 30 ans ou des 30 ans et plus.
    private NiveauRisque determineNiveau(int age, String genre, int nombreDeDeclencheurs) {
        if (nombreDeDeclencheurs == 0) {
            return NiveauRisque.NONE;
        }
        if (age < ConstantesRisque.AGE_PIVOT) {
            return niveauPatientJeune(genre, nombreDeDeclencheurs);
        }
        return niveauPatientTrenteEtPlus(nombreDeDeclencheurs);
    }

    // Applique les seuils des moins de 30 ans, qui dépendent du genre.
    private NiveauRisque niveauPatientJeune(String genre, int nombreDeDeclencheurs) {
        boolean masculin = ConstantesRisque.GENRE_MASCULIN.equalsIgnoreCase(genre);
        int seuilEarlyOnset = masculin
                ? ConstantesRisque.SEUIL_EARLY_ONSET_HOMME_JEUNE
                : ConstantesRisque.SEUIL_EARLY_ONSET_FEMME_JEUNE;
        int seuilInDanger = masculin
                ? ConstantesRisque.SEUIL_IN_DANGER_HOMME_JEUNE
                : ConstantesRisque.SEUIL_IN_DANGER_FEMME_JEUNE;

        if (nombreDeDeclencheurs >= seuilEarlyOnset) {
            return NiveauRisque.EARLY_ONSET;
        }
        if (nombreDeDeclencheurs >= seuilInDanger) {
            return NiveauRisque.IN_DANGER;
        }
        return NiveauRisque.NONE;
    }

    // Applique les seuils des 30 ans et plus, identiques pour les deux genres.
    private NiveauRisque niveauPatientTrenteEtPlus(int nombreDeDeclencheurs) {
        if (nombreDeDeclencheurs >= ConstantesRisque.SEUIL_EARLY_ONSET_TRENTE_ET_PLUS) {
            return NiveauRisque.EARLY_ONSET;
        }
        if (nombreDeDeclencheurs >= ConstantesRisque.SEUIL_IN_DANGER_TRENTE_ET_PLUS) {
            return NiveauRisque.IN_DANGER;
        }
        if (nombreDeDeclencheurs >= ConstantesRisque.SEUIL_BORDERLINE) {
            return NiveauRisque.BORDERLINE;
        }
        return NiveauRisque.NONE;
    }
}
