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
