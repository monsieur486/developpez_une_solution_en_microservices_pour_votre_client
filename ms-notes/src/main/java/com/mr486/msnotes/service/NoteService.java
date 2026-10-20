package com.mr486.msnotes.service;

import com.mr486.commun.dto.NoteForm;
import com.mr486.msnotes.model.Note;
import com.mr486.msnotes.repository.NoteRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Règles de gestion des notes : lecture de l'historique et enregistrement daté.
 *
 * <p>La date d'une note est attribuée ici, par l'horloge injectée, et jamais par l'appelant.</p>
 *
 * <p><b>Exemple :</b> {@code save(3L, form)} enregistre la note pour le patient 3, datée de
 * l'instant présent.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NoteService {
    private final NoteRepository noteRepository;

    private final Clock horloge;

    /**
     * Renvoie l'historique d'un patient, de la note la plus récente à la plus ancienne.
     *
     * <p><b>Exemple :</b> avec le jeu de démonstration, {@code findByPatientId(2L)} renvoie les
     * 2 notes de {@code TestBorderline}.</p>
     *
     * @param patientId identifiant du patient
     * @return ses notes, éventuellement aucune
     */
    public List<Note> findByPatientId(Long patientId) {
        List<Note> notes = noteRepository.findByPatientIdOrderByCreatedDateDesc(patientId);
        if (notes.isEmpty()) {
            log.debug("aucune note enregistrée pour le patient {}", patientId);
        }
        return notes;
    }

    /**
     * Enregistre une nouvelle note, datée de l'instant présent.
     *
     * <p><b>Exemple :</b> {@code save(3L, NoteForm.builder().content("Poids en hausse").build())}
     * renvoie la note avec son identifiant MongoDB et sa date.</p>
     *
     * @param patientId identifiant du patient, issu de l'URL
     * @param noteForm  note saisie
     * @return la note enregistrée
     */
    public Note save(Long patientId, NoteForm noteForm) {
        Note enregistree = noteRepository.save(construitNote(patientId, noteForm));
        log.info("note créée pour le patient {}", patientId);
        return enregistree;
    }

    // Construit le document à enregistrer ; le texte est recopié tel quel.
    private Note construitNote(Long patientId, NoteForm noteForm) {
        log.debug("construction d'une note pour le patient {}", patientId);
        return Note.builder()
                .patientId(patientId)
                .content(noteForm.getContent())
                .createdDate(Instant.now(horloge))
                .build();
    }
}
