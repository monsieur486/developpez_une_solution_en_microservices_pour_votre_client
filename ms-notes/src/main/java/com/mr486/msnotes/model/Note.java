package com.mr486.msnotes.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

/**
 * Note médicale rattachée à un patient, persistée dans la collection MongoDB
 * {@code notes}.
 *
 * <p><b>Exemple :</b> une note porte l'identifiant du patient, le contenu textuel
 * et sa date de création ; seul le contenu est exposé en JSON (les autres champs
 * sont annotés {@code @JsonIgnore}).</p>
 */
@Document(collection = "notes")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Note {

    @Id
    @JsonIgnore
    private String id;

    @JsonIgnore
    @Indexed
    private Long patientId;

    private String content;

    @JsonIgnore
    private Date createdDate;

}
