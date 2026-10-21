package com.mr486.msnotes.dbchangelogs;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * Migration Mongock qui insère les 9 notes de démonstration des cas de test.
 *
 * <p>Elle ne s'exécute qu'avec le profil {@code demo} ({@code MONGOCK_PROFILES=demo}). Les notes
 * visent les patients 1 à 4, c'est-à-dire les identifiants qu'attribue PostgreSQL aux 4 patients
 * de démonstration de {@code ms-patients}, insérés dans cet ordre. Une collection déjà remplie
 * n'est pas touchée.</p>
 *
 * <p><b>Exemple :</b> sur une base vierge avec le profil {@code demo}, le patient 4
 * ({@code TestEarlyOnset}) reçoit 4 notes ; avec le profil {@code prod}, aucune note n'est insérée.</p>
 */
@ChangeUnit(id = "seed-demo-notes", order = "001", author = "mr486")
@Profile("demo")
@Slf4j
// org.bson.Document est le type imposé par le pilote MongoDB : PMD y voit à tort une Map concrète.
@SuppressWarnings("PMD.LooseCoupling")
public class DataBaseChangeLog {
    private static final String COLLECTION = "notes";

    private static final String CHAMP_PATIENT = "patientId";

    private static final String CHAMP_CONTENU = "content";

    private static final String CHAMP_DATE = "createdDate";

    private static final long PATIENT_SANS_RISQUE = 1L;

    private static final long PATIENT_RISQUE_LIMITE = 2L;

    private static final long PATIENT_EN_DANGER = 3L;

    private static final long PATIENT_RISQUE_PRECOCE = 4L;

    private static final List<NoteDeDemonstration> MODELES = List.of(
            new NoteDeDemonstration(PATIENT_SANS_RISQUE,
                    "Le patient déclare qu'il 'se sent très bien' Poids égal ou inférieur au "
                            + "poids recommandé"),
            new NoteDeDemonstration(PATIENT_RISQUE_LIMITE,
                    "Le patient déclare qu'il ressent beaucoup de stress au travail Il se plaint "
                            + "également que son audition est anormale dernièrement"),
            new NoteDeDemonstration(PATIENT_RISQUE_LIMITE,
                    "Le patient déclare avoir fait une réaction aux médicaments au cours des 3 "
                            + "derniers mois Il remarque également que son audition continue "
                            + "d'être anormale"),
            new NoteDeDemonstration(PATIENT_EN_DANGER,
                    "Le patient déclare qu'il fume depuis peu"),
            new NoteDeDemonstration(PATIENT_EN_DANGER,
                    "Le patient déclare qu'il est fumeur et qu'il a cessé de fumer l'année "
                            + "dernière Il se plaint également de crises d’apnée respiratoire "
                            + "anormales Tests de laboratoire indiquant un taux de cholestérol "
                            + "LDL élevé"),
            new NoteDeDemonstration(PATIENT_RISQUE_PRECOCE,
                    "Le patient déclare qu'il lui est devenu difficile de monter les escaliers Il "
                            + "se plaint également d’être essoufflé Tests de laboratoire indiquant "
                            + "que les anticorps sont élevés Réaction aux médicaments"),
            new NoteDeDemonstration(PATIENT_RISQUE_PRECOCE,
                    "Le patient déclare qu'il a mal au dos lorsqu'il reste assis pendant longtemps"),
            new NoteDeDemonstration(PATIENT_RISQUE_PRECOCE,
                    "Le patient déclare avoir commencé à fumer depuis peu Hémoglobine A1C "
                            + "supérieure au niveau recommandé"),
            new NoteDeDemonstration(PATIENT_RISQUE_PRECOCE,
                    "Taille, Poids, Cholestérol, Vertige et Réaction"));

    private final MongoTemplate mongoTemplate;

    /**
     * Construit la migration avec l'accès à MongoDB fourni par Mongock.
     *
     * <p><b>Exemple :</b> Mongock l'instancie lui-même au démarrage ; aucun appel direct n'est prévu.</p>
     *
     * @param mongoTemplate accès à la base des notes
     */
    public DataBaseChangeLog(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * Insère les notes de démonstration si la collection est vide.
     *
     * <p><b>Exemple :</b> au premier démarrage avec {@code demo}, 9 notes sont insérées, à une seconde
     * d'écart pour garder l'ordre des cas de test, la dernière une seconde avant le démarrage.</p>
     */
    @Execution
    public void seedDemoNotesIfEmpty() {
        if (mongoTemplate.getCollection(COLLECTION).countDocuments() > 0) {
            log.info("notes de démonstration déjà présentes, insertion ignorée");
            return;
        }

        List<Document> notes = construitLesNotesDeDemonstration();
        mongoTemplate.getCollection(COLLECTION).insertMany(notes);
        log.info("{} notes de démonstration insérées", notes.size());
    }

    /**
     * Annule la migration en vidant la collection, si l'insertion a échoué.
     *
     * <p><b>Exemple :</b> Mongock l'appelle lui-même lorsque {@link #seedDemoNotesIfEmpty()} lève
     * une exception ; elle n'efface rien sinon.</p>
     */
    @RollbackExecution
    public void rollback() {
        mongoTemplate.getCollection(COLLECTION).deleteMany(new Document());
        log.warn("notes de démonstration supprimées par annulation de la migration");
    }

    // Construit les 9 documents de démonstration, à une seconde d'écart et tous dans le passé :
    // une note saisie juste après le démarrage reste ainsi la plus récente de l'historique.
    private List<Document> construitLesNotesDeDemonstration() {
        Instant maintenant = Instant.now();
        List<Document> notes = new ArrayList<>();
        for (NoteDeDemonstration modele : MODELES) {
            notes.add(new Document()
                    .append(CHAMP_PATIENT, modele.patientId())
                    .append(CHAMP_CONTENU, modele.contenu())
                    .append(CHAMP_DATE, maintenant.minusSeconds(MODELES.size() - notes.size())));
        }
        return notes;
    }

    // Patient visé et texte d'une note de démonstration.
    private record NoteDeDemonstration(long patientId, String contenu) {
    }
}
