package com.mr486.msrisque.service;

import com.mr486.msrisque.client.NoteClient;
import com.mr486.msrisque.dto.Note;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Récupère les notes médicales d'un patient auprès du microservice ms-notes.
 *
 * <p><b>Exemple :</b> getNotesByPatientId(7L) retourne la liste des notes du
 * patient d'identifiant 7.</p>
 */
@Service
@RequiredArgsConstructor
public class NotesService {

    private final NoteClient noteClient;

    /**
     * Récupère les notes médicales d'un patient.
     *
     * <p><b>Exemple :</b> getNotesByPatientId(7L) retourne les notes du patient 7
     * (liste vide si le patient n'a aucune note).</p>
     *
     * @param patientId identifiant du patient
     * @return la liste des notes du patient
     */
    public List<Note> getNotesByPatientId(Long patientId) {
        return noteClient.getNotesByPatientId(patientId);
    }
}
