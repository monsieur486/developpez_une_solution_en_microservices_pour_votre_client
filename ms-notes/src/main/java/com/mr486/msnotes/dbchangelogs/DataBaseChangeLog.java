package com.mr486.msnotes.dbchangelogs;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@ChangeUnit(id = "seed-demo-notes", order = "001", author = "mr486")
public class DataBaseChangeLog {

  private final MongoTemplate mongoTemplate;

  public DataBaseChangeLog(MongoTemplate mongoTemplate) {
    this.mongoTemplate = mongoTemplate;
  }

  @Execution
  public void seedDemoNotesIfEmpty() {
    System.out.println("[MONGOCK] Migration appelée");
    if (mongoTemplate.getCollection("notes").countDocuments() > 0) return;

    LocalDateTime now = LocalDateTime.now();
    List<org.bson.Document> notes = List.of(
            new org.bson.Document()
                    .append("patientId", 1L)
                    .append("content", "Le patient déclare qu'il 'se sent très bien' Poids égal ou inférieur au poids recommandé")
                    .append("createdDate", toDate(now)),
            new org.bson.Document()
                    .append("patientId", 2L)
                    .append("content", "Le patient déclare qu'il ressent beaucoup de stress au travail Il se plaint également que son audition est anormale dernièrement")
                    .append("createdDate", toDate(now.plusSeconds(1))),
            new org.bson.Document()
                    .append("patientId", 2L)
                    .append("content", "Le patient déclare avoir fait une réaction aux médicaments au cours des 3 derniers mois Il remarque également que son audition continue d'être anormale")
                    .append("createdDate", toDate(now.plusSeconds(2))),
            new org.bson.Document()
                    .append("patientId", 3L)
                    .append("content", "Le patient déclare qu'il fume depuis peu")
                    .append("createdDate", toDate(now.plusSeconds(3))),
            new org.bson.Document()
                    .append("patientId", 3L)
                    .append("content", "Le patient déclare qu'il est fumeur et qu'il a cessé de fumer l'année dernière Il se plaint également de crises d’apnée respiratoire anormales Tests de laboratoire indiquant un taux de cholestérol LDL élevé")
                    .append("createdDate", toDate(now.plusSeconds(4))),
            new org.bson.Document()
                    .append("patientId", 4L)
                    .append("content", "Le patient déclare qu'il lui est devenu difficile de monter les escaliers Il se plaint également d’être essoufflé Tests de laboratoire indiquant que les anticorps sont élevés Réaction aux médicaments")
                    .append("createdDate", toDate(now.plusSeconds(5))),
            new org.bson.Document()
                    .append("patientId", 4L)
                    .append("content", "Le patient déclare qu'il a mal au dos lorsqu'il reste assis pendant longtemps")
                    .append("createdDate", toDate(now.plusSeconds(6))),
            new org.bson.Document()
                    .append("patientId", 4L)
                    .append("content", "Le patient déclare avoir commencé à fumer depuis peu Hémoglobine A1C supérieure au niveau recommandé")
                    .append("createdDate", toDate(now.plusSeconds(7))),
            new org.bson.Document()
                    .append("patientId", 4L)
                    .append("content", "Taille, Poids, Cholestérol, Vertige et Réaction")
                    .append("createdDate", toDate(now.plusSeconds(8)))
    );
    mongoTemplate.getCollection("notes").insertMany(notes);
  }

  @RollbackExecution
  public void rollback() {
    mongoTemplate.getCollection("notes").deleteMany(new org.bson.Document());
  }

  private Date toDate(LocalDateTime ldt) {
    return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
  }
}
