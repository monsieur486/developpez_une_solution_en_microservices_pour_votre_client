package com.mr486.msrisque.client;

import com.mr486.msrisque.configuration.FeignConfiguration;
import com.mr486.msrisque.dto.Note;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * Client Feign vers le microservice ms-notes.
 *
 * <p><b>Exemple :</b> getNotesByPatientId(7L) appelle GET /patients/7/notes et
 * retourne les notes du patient 7.</p>
 */
@FeignClient(name = "ms-notes", configuration = FeignConfiguration.class)
public interface NoteClient {

    /**
     * Récupère les notes médicales d'un patient.
     *
     * <p><b>Exemple :</b> getNotesByPatientId(7L) retourne les notes du patient 7.</p>
     *
     * @param id identifiant du patient
     * @return la liste des notes du patient
     */
    @GetMapping("/patients/{id}/notes")
    List<Note> getNotesByPatientId(@PathVariable Long id);
}
