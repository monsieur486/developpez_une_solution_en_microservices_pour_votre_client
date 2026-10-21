package com.mr486.msnotes.model;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Note rédigée par un praticien sur un patient, stockée dans la collection {@code notes}.
 *
 * <p>Le patient n'est désigné que par son identifiant : sa fiche appartient à {@code ms-patients},
 * et aucun des deux microservices ne lit la base de l'autre. Le texte est conservé tel quel, casse
 * comprise, car l'évaluation du risque y recherche des termes déclencheurs.</p>
 *
 * <p><b>Exemple :</b> la note de démonstration « Le patient déclare qu'il fume depuis peu » porte
 * {@code patientId = 3}.</p>
 */
@Document(collection = "notes")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Note {
    /** Identifiant attribué par MongoDB. */
    @Id
    private String id;

    /**
     * Identifiant du patient dans {@code ms-patients}, sur lequel l'historique filtre.
     *
     * <p>{@code @Indexed} ne crée l'index que si {@code spring.data.mongodb.auto-index-creation} est
     * activé, ce qui n'est pas le cas : la collection n'a aujourd'hui que l'index {@code _id}. Voir
     * les recommandations Green Code du README.</p>
     */
    @Indexed
    private Long patientId;

    /** Texte libre de la note. */
    private String content;

    /** Instant d'enregistrement, qui ordonne l'historique. */
    private Instant createdDate;
}
