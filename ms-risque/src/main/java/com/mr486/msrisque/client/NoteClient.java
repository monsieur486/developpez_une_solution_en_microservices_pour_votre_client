package com.mr486.msrisque.client;

import com.mr486.msrisque.configuration.FeignConfiguration;
import com.mr486.msrisque.dto.Note;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "ms-notes", configuration = FeignConfiguration.class)
public interface NoteClient {
  @GetMapping("/patients/{id}/notes")
  List<Note> getNotesByPatientId(@PathVariable Long id);
}
