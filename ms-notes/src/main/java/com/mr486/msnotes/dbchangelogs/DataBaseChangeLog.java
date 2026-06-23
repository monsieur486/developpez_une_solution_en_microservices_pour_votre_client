package com.mr486.msnotes.dbchangelogs;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 * Migration Mongock qui amorce la collection {@code notes} avec un jeu de notes de
 * démonstration, uniquement si elle est vide.
 *
 * <p><b>Exemple :</b> au premier démarrage, seedDemoNotesIfEmpty() insère neuf notes
 * réparties sur quatre patients ; aux démarrages suivants, la collection n'étant plus
 * vide, la migration ne fait rien.</p>
 */
@ChangeUnit(id = "seed-demo-notes", order = "001", author = "mr486")
public class DataBaseChangeLog {

    private static final Logger log = LoggerFactory.getLogger(DataBaseChangeLog.class);

    private final MongoTemplate mongoTemplate;

    public DataBaseChangeLog(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * Insère les notes de démonstration si la collection {@code notes} est vide.
     *
     * <p><b>Exemple :</b> sur une base vierge, seedDemoNotesIfEmpty() crée neuf
     * notes ; si des notes existent déjà, la méthode sort sans rien insérer.</p>
     */
    @Execution
    public void seedDemoNotesIfEmpty() {
        log.info("[MONGOCK] Migration appelée");
        if (mongoTemplate.getCollection("notes").countDocuments() > 0) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        List<Document> notes = List.of(
                new Document()
                        .append("patientId", 1L)
                        .append("content", "Le patient déclare qu'il 'se sent très bien' Poids égal ou inférieur au poids recommandé")
                        .append("createdDate", toDate(now)),
                new Document()
                        .append("patientId", 2L)
                        .append("content", "Le patient déclare qu'il ressent beaucoup de stress au travail Il se plaint également que son audition est anormale dernièrement")
                        .append("createdDate", toDate(now.plusSeconds(1))),
                new Document()
                        .append("patientId", 2L)
                        .append("content", "Le patient déclare avoir fait une réaction aux médicaments au cours des 3 derniers mois Il remarque également que son audition continue d'être anormale")
                        .append("createdDate", toDate(now.plusSeconds(2))),
                new Document()
                        .append("patientId", 3L)
                        .append("content", "Le patient déclare qu'il fume depuis peu")
                        .append("createdDate", toDate(now.plusSeconds(3))),
                new Document()
                        .append("patientId", 3L)
                        .append("content", "Le patient déclare qu'il est fumeur et qu'il a cessé de fumer l'année dernière Il se plaint également de crises d’apnée respiratoire anormales Tests de laboratoire indiquant un taux de cholestérol LDL élevé")
                        .append("createdDate", toDate(now.plusSeconds(4))),
                new Document()
                        .append("patientId", 4L)
                        .append("content", "Le patient déclare qu'il lui est devenu difficile de monter les escaliers Il se plaint également d’être essoufflé Tests de laboratoire indiquant que les anticorps sont élevés Réaction aux médicaments")
                        .append("createdDate", toDate(now.plusSeconds(5))),
                new Document()
                        .append("patientId", 4L)
                        .append("content", "Le patient déclare qu'il a mal au dos lorsqu'il reste assis pendant longtemps")
                        .append("createdDate", toDate(now.plusSeconds(6))),
                new Document()
                        .append("patientId", 4L)
                        .append("content", "Le patient déclare avoir commencé à fumer depuis peu Hémoglobine A1C supérieure au niveau recommandé")
                        .append("createdDate", toDate(now.plusSeconds(7))),
                new Document()
                        .append("patientId", 4L)
                        .append("content", "Taille, Poids, Cholestérol, Vertige et Réaction")
                        .append("createdDate", toDate(now.plusSeconds(8)))
        );

        // Ajoute le discriminant _class attendu par Spring Data avant insertion.
        notes.forEach(doc -> doc.append("_class", "com.mr486.msnotes.model.Note"));

        mongoTemplate.getCollection("notes").insertMany(notes);
    }

    /**
     * Annule la migration en vidant la collection {@code notes}.
     *
     * <p><b>Exemple :</b> rollback() supprime toutes les notes insérées par cette
     * migration.</p>
     */
    @RollbackExecution
    public void rollback() {
        mongoTemplate.getCollection("notes").deleteMany(new Document());
    }

    // Convertit un LocalDateTime en Date selon le fuseau horaire du système.
    private Date toDate(LocalDateTime ldt) {
        return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
    }
}
