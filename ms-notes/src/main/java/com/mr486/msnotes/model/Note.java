package com.mr486.msnotes.model;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "notes")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Note {
    @Id
    private String id;

    @Indexed
    private Long patientId;

    private String content;

    private Instant createdDate;
}
