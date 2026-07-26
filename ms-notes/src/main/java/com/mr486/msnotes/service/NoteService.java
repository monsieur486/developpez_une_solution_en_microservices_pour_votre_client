package com.mr486.msnotes.service;

import com.mr486.msnotes.configuration.ConstantesApplication;
import com.mr486.msnotes.dto.NoteDto;
import com.mr486.msnotes.model.Note;
import com.mr486.msnotes.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * Gère les notes des patients : lecture et création.
 *
 * <p><b>Exemple :</b> findByPatientId(2L) retourne les notes du patient 2 ;
 * save(2L, dto) crée une note datée pour ce patient.</p>
 */
@Service
@RequiredArgsConstructor
public class NoteService {

    private final NoteRepository noteRepository;

    /**
     * Retourne les notes d'un patient, de la plus récente à la plus ancienne.
     *
     * <p><b>Exemple :</b> findByPatientId(2L) retourne la liste des notes du
     * patient 2 (vide si aucune).</p>
     *
     * @param patientId identifiant du patient
     * @return les notes du patient triées par date décroissante
     */
    public List<Note> findByPatientId(Long patientId) {
        return noteRepository.findByPatientIdOrderByCreatedDateDesc(patientId);
    }

    /**
     * Retourne une page des notes d'un patient, de la plus récente à la plus
     * ancienne (5 notes par page).
     *
     * <p><b>Exemple :</b> findByPatientId(2L, 0) retourne les 5 dernières notes
     * du patient 2 ; avec 12 notes, la page porte totalPages=3.</p>
     *
     * @param patientId identifiant du patient
     * @param page      numéro de la page demandée (à partir de 0)
     * @return la page de notes correspondante
     */
    public Page<Note> findByPatientId(Long patientId, int page) {
        PageRequest pageRequest = PageRequest.of(page, ConstantesApplication.TAILLE_PAGE_NOTES,
                Sort.by(Sort.Direction.DESC, "createdDate"));
        return noteRepository.findByPatientId(patientId, pageRequest);
    }

    /**
     * Crée une note datée pour un patient à partir des données reçues.
     *
     * <p><b>Exemple :</b> save(2L, dto) persiste une nouvelle note pour le patient
     * 2 et retourne l'entité enregistrée (avec sa date de création).</p>
     *
     * @param patientId identifiant du patient concerné
     * @param noteDto   contenu de la note à créer
     * @return la note créée
     */
    public Note save(Long patientId, NoteDto noteDto) {
        Note note = Note.builder()
                .patientId(patientId)
                .content(noteDto.getContent())
                .createdDate(new Date())
                .build();
        return noteRepository.save(note);
    }

}
