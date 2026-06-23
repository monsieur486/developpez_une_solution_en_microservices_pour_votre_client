package com.mr486.msrisque.service;

import com.mr486.msrisque.dto.Note;
import com.mr486.msrisque.dto.Patient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EvaluationServiceTest {

    // Date de référence figée : les âges sont calculés par rapport à elle.
    private static final LocalDate REFERENCE = LocalDate.of(2025, 6, 15);

    @Mock
    private PatientService patientService;

    @Mock
    private NotesService notesService;

    private EvaluationService evaluationService;

    @BeforeEach
    void init() {
        Clock horloge = Clock.fixed(REFERENCE.atStartOfDay(ZoneOffset.UTC).toInstant(), ZoneOffset.UTC);
        evaluationService = new EvaluationService(patientService, notesService, horloge);
    }

    // Construit un patient d'un genre et d'un âge donnés (date de naissance déduite).
    private Patient patient(String genre, int age) {
        return Patient.builder()
                .gender(genre)
                .birthDate(REFERENCE.minusYears(age))
                .build();
    }

    // Transforme des contenus en liste de notes (un contenu null est autorisé).
    private List<Note> notes(String... contenus) {
        return Arrays.stream(contenus)
                .map(contenu -> Note.builder().content(contenu).build())
                .toList();
    }

    // Configure les mocks pour le patient d'id 1 et son niveau évalué.
    private String niveau(Patient patient, List<Note> notes) {
        when(patientService.getPatientById(1L)).thenReturn(patient);
        when(notesService.getNotesByPatientId(1L)).thenReturn(notes);
        return evaluationService.evalueRisque(1L).getLevel();
    }

    @Test
    void aucunDeclencheur_retourneNone() {
        assertThat(niveau(patient("M", 40), notes("patient en bonne forme"))).isEqualTo("None");
    }

    @Test
    void borderline_quandPlusDe30AnsEt2Declencheurs() {
        assertThat(niveau(patient("M", 40), notes("taille poids"))).isEqualTo("Borderline");
    }

    @Test
    void borderline_borneHaute5Declencheurs() {
        assertThat(niveau(patient("F", 45), notes("taille poids fumeur anormal cholestérol")))
                .isEqualTo("Borderline");
    }

    @Test
    void inDanger_jeuneHomme3Declencheurs() {
        assertThat(niveau(patient("M", 25), notes("taille poids fumeur"))).isEqualTo("In Danger");
    }

    @Test
    void inDanger_jeuneFemme4Declencheurs() {
        assertThat(niveau(patient("F", 25), notes("taille poids fumeur anormal"))).isEqualTo("In Danger");
    }

    @Test
    void inDanger_plusDe30Ans6Declencheurs() {
        assertThat(niveau(patient("M", 40), notes("taille poids fumeur anormal cholestérol vertige")))
                .isEqualTo("In Danger");
    }

    @Test
    void inDanger_plusDe30Ans7Declencheurs() {
        assertThat(niveau(patient("F", 40), notes("taille poids fumeur anormal cholestérol vertige rechute")))
                .isEqualTo("In Danger");
    }

    @Test
    void earlyOnset_jeuneHomme5Declencheurs() {
        assertThat(niveau(patient("M", 25), notes("taille poids fumeur anormal cholestérol")))
                .isEqualTo("Early onset");
    }

    @Test
    void earlyOnset_jeuneFemme7Declencheurs() {
        assertThat(niveau(patient("F", 25), notes("taille poids fumeur anormal cholestérol vertige rechute")))
                .isEqualTo("Early onset");
    }

    @Test
    void earlyOnset_plusDe30Ans8Declencheurs() {
        assertThat(niveau(patient("M", 40),
                notes("taille poids fumeur anormal cholestérol vertige rechute réaction")))
                .isEqualTo("Early onset");
    }

    @Test
    void none_quandConditionsNonRemplies() {
        // Jeune homme avec 2 déclencheurs : ni borderline (âge), ni danger, ni early onset.
        assertThat(niveau(patient("M", 25), notes("taille poids"))).isEqualTo("None");
    }

    @Test
    void noteAuContenuNull_estIgnoree() {
        // Seule la première note compte (2 déclencheurs) : la note null est ignorée.
        assertThat(niveau(patient("M", 40), notes("taille poids", null))).isEqualTo("Borderline");
    }

    @Test
    void age30Pile_retourneNone_comportementActuel() {
        // 30 ans n'est ni < 30 ni > 30 : aucune branche métier ne s'applique.
        assertThat(niveau(patient("M", 30), notes("taille poids fumeur anormal cholestérol vertige")))
                .isEqualTo("None");
    }
}
