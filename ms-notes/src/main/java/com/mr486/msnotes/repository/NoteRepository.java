package com.mr486.msnotes.repository;

import com.mr486.msnotes.model.Note;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NoteRepository extends MongoRepository<Note, String> {
    List<Note> findByPatientIdOrderByCreatedDateDesc(Long patientId);
}
