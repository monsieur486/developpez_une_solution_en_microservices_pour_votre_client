package com.mr486.msrisque.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.mr486.commun.dto.NoteDto;
import com.mr486.commun.dto.PatientDto;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("Évaluation du risque de diabète")
class EvaluationServiceTest {
    private static final LocalDate DATE_REFERENCE = LocalDate.of(2026, 8, 1);

    private static final Long PATIENT_ID = 1L;

    private static final List<String> TERMES = List.of(
            "Hémoglobine A1C",
            "Microalbumine",
            "Taille",
            "Poids",
            "Fumeur",
            "Anormal",
            "Cholestérol",
            "Vertige",
            "Rechute",
            "Réaction",
            "Anticorps");

    @Mock
    private PatientService patientService;

    @Mock
    private NotesService notesService;

    private EvaluationService evaluationService;

    @BeforeEach
    void initialiseLeServiceAvecUneHorlogeFigee() {
        Clock horlogeFigee = Clock.fixed(
                DATE_REFERENCE.atStartOfDay(ZoneOffset.UTC).toInstant(), ZoneOffset.UTC);
        evaluationService = new EvaluationService(patientService, notesService, horlogeFigee);
    }

    // Chaque ligne est une borne des règles du cahier des charges. Les trous des règles sont
    // tranchés ici : 30 ans pile suit les règles des plus de 30 ans ; un patient de moins de
    // 30 ans sous le seuil In Danger, ou de plus de 30 ans avec un seul terme, reste à None.
    @ParameterizedTest(name = "{0} ans, genre {1}, {2} déclencheur(s) : {3}")
    @CsvSource({
        "40, F, 0, None",
        "40, M, 1, None",
        "30, F, 2, Borderline",
        "40, M, 5, Borderline",
        "40, M, 6, In Danger",
        "40, F, 7, In Danger",
        "40, F, 8, Early onset",
        "25, M, 2, None",
        "25, M, 3, In Danger",
        "25, M, 4, In Danger",
        "25, M, 5, Early onset",
        "25, F, 2, None",
        "25, F, 3, None",
        "25, F, 4, In Danger",
        "25, F, 6, In Danger",
        "25, F, 7, Early onset"})
    @DisplayName("les seuils dépendent de l'âge, du genre et du nombre de déclencheurs")
    void seuils_donnentLeNiveauAttendu(int age, String genre, int nombreDeDeclencheurs, String attendu) {
        donneUnPatient(age, genre);
        donneDesNotesAvec(nombreDeDeclencheurs);

        assertThat(evaluationService.evaluationDuRisque(PATIENT_ID)).isEqualTo(attendu);
    }

    // Le patient a plus de 30 ans : deux termes distincts donnent Borderline, un seul None.
    // Chaque cas dit pourquoi il existe.
    @ParameterizedTest(name = "{0}")
    @MethodSource("casDeComptage")
    @DisplayName("les termes déclencheurs sont comptés une fois chacun, quelle que soit leur graphie")
    void comptage_releveLesTermesDistincts(String pourquoi, List<NoteDto> notes, String attendu) {
        donneUnPatient(40, "F");
        when(notesService.getNotesByPatientId(PATIENT_ID)).thenReturn(notes);

        assertThat(evaluationService.evaluationDuRisque(PATIENT_ID)).isEqualTo(attendu);
    }

    static Stream<Arguments> casDeComptage() {
        return Stream.of(
                Arguments.of("la casse des notes est ignorée (« Hémoglobine A1C »)", List.of(
                        uneNote("Hémoglobine A1C supérieure au niveau recommandé"),
                        uneNote("MICROALBUMINE détectée")), "Borderline"),
                Arguments.of("les termes répartis sur plusieurs notes sont cumulés", List.of(
                        uneNote("Taille et Poids relevés"),
                        uneNote("Patient Fumeur"),
                        uneNote("Cholestérol élevé"),
                        uneNote("Vertige signalé"),
                        uneNote("Réaction aux médicaments")), "In Danger"),
                Arguments.of("un symptôme noté à chaque visite ne compte qu'une fois", List.of(
                        uneNote("Patient Fumeur"),
                        uneNote("Toujours fumeur")), "None"),
                Arguments.of("« Fumeuse » est reconnu comme le terme « Fumeur »", List.of(
                        uneNote("La patiente est fumeuse"),
                        uneNote("Cholestérol élevé")), "Borderline"),
                Arguments.of("« Fumeur » et « Fumeuse » sont deux graphies d'un seul terme", List.of(
                        uneNote("Ancienne fumeuse"),
                        uneNote("Se déclare fumeur occasionnel")), "None"),
                Arguments.of("une note sans contenu est ignorée, sans faire échouer l'évaluation", List.of(
                        uneNote("Cholestérol élevé"),
                        uneNote(null),
                        uneNote("Poids en hausse")), "Borderline"));
    }

    // Recette du cahier des charges : les quatre patients de démonstration, avec leurs notes
    // reprises mot pour mot des données de ms-patients et ms-notes, doivent donner le niveau
    // annoncé par leur nom.
    @ParameterizedTest(name = "{0} : {3}")
    @MethodSource("casOfficiels")
    @DisplayName("les quatre cas officiels donnent le niveau attendu")
    void casOfficiels_donnentLeNiveauAttendu(
            String nom, LocalDate dateDeNaissance, String genre, String attendu, List<String> notes) {
        when(patientService.getPatientById(PATIENT_ID)).thenReturn(new PatientDto(
                PATIENT_ID, "Test", nom, dateDeNaissance, genre, null, null));
        when(notesService.getNotesByPatientId(PATIENT_ID))
                .thenReturn(notes.stream().map(EvaluationServiceTest::uneNote).toList());

        assertThat(evaluationService.evaluationDuRisque(PATIENT_ID)).isEqualTo(attendu);
    }

    static Stream<Arguments> casOfficiels() {
        return Stream.of(
                Arguments.of("TestNone", LocalDate.of(1966, 12, 31), "F", "None", List.of(
                        "Le patient déclare qu'il 'se sent très bien' Poids égal ou inférieur au "
                                + "poids recommandé")),
                Arguments.of("TestBorderline", LocalDate.of(1945, 6, 24), "M", "Borderline", List.of(
                        "Le patient déclare qu'il ressent beaucoup de stress au travail Il se plaint "
                                + "également que son audition est anormale dernièrement",
                        "Le patient déclare avoir fait une réaction aux médicaments au cours des 3 "
                                + "derniers mois Il remarque également que son audition continue "
                                + "d'être anormale")),
                Arguments.of("TestInDanger", LocalDate.of(2004, 6, 18), "M", "In Danger", List.of(
                        "Le patient déclare qu'il fume depuis peu",
                        "Le patient déclare qu'il est fumeur et qu'il a cessé de fumer l'année "
                                + "dernière Il se plaint également de crises d’apnée respiratoire "
                                + "anormales Tests de laboratoire indiquant un taux de cholestérol "
                                + "LDL élevé")),
                Arguments.of("TestEarlyOnset", LocalDate.of(2002, 6, 28), "F", "Early onset", List.of(
                        "Le patient déclare qu'il lui est devenu difficile de monter les escaliers Il "
                                + "se plaint également d’être essoufflé Tests de laboratoire indiquant "
                                + "que les anticorps sont élevés Réaction aux médicaments",
                        "Le patient déclare qu'il a mal au dos lorsqu'il reste assis pendant longtemps",
                        "Le patient déclare avoir commencé à fumer depuis peu Hémoglobine A1C "
                                + "supérieure au niveau recommandé",
                        "Taille, Poids, Cholestérol, Vertige et Réaction")));
    }

    @Test
    @DisplayName("un anniversaire non encore passé cette année ne compte pas dans l'âge")
    void anniversaireNonEncorePasse_neCompteQueLesAnneesRevolues() {
        when(patientService.getPatientById(PATIENT_ID)).thenReturn(new PatientDto(
                PATIENT_ID, "Test", "Patient", DATE_REFERENCE.minusYears(30).plusDays(1),
                "M", null, null));
        donneDesNotesAvec(3);

        assertThat(evaluationService.evaluationDuRisque(PATIENT_ID)).isEqualTo("In Danger");
    }

    private void donneUnPatient(int age, String genre) {
        when(patientService.getPatientById(PATIENT_ID)).thenReturn(new PatientDto(
                PATIENT_ID, "Test", "Patient", DATE_REFERENCE.minusYears(age), genre, null, null));
    }

    private void donneDesNotesAvec(int nombreDeDeclencheurs) {
        String contenu = String.join(", ", TERMES.subList(0, nombreDeDeclencheurs));
        when(notesService.getNotesByPatientId(PATIENT_ID)).thenReturn(List.of(uneNote(contenu)));
    }

    private static NoteDto uneNote(String contenu) {
        return NoteDto.builder().content(contenu).build();
    }
}
