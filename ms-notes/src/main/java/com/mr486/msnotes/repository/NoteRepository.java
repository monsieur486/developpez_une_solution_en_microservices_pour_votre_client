package com.mr486.msnotes.repository;

import com.mr486.msnotes.model.Note;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NoteRepository extends MongoRepository<Note, UUID> {

  List<Note> findByPatientIdOrderByCreatedDateDesc(Long patientId);
}
