package com.mr486.msnotes.mapper;

import com.mr486.commun.dto.NoteDto;
import com.mr486.msnotes.model.Note;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Convertit les notes du document MongoDB vers l'objet renvoyé par l'API.
 *
 * <p>Ni l'identifiant MongoDB ni le patient ne sortent : l'appelant connaît déjà le patient,
 * puisqu'il l'a désigné dans l'URL.</p>
 *
 * <p><b>Exemple :</b> {@code versDto(note)} produit le {@link NoteDto} renvoyé par
 * {@code GET /patients/3/notes}.</p>
 */
@Component
public class NoteMapper {
    /**
     * Construit la représentation API d'une note.
     *
     * <p><b>Exemple :</b> une note « Poids en hausse » enregistrée le 2026-10-20T16:13:39Z donne un
     * DTO portant ce texte et cette date.</p>
     *
     * @param note document lu en base
     * @return le DTO avec le texte et la date de la note
     */
    public NoteDto versDto(Note note) {
        return NoteDto.builder()
                .content(note.getContent())
                .createdDate(note.getCreatedDate())
                .build();
    }

    /**
     * Construit la représentation API d'un historique, dans le même ordre.
     *
     * <p><b>Exemple :</b> une liste vide donne une liste vide.</p>
     *
     * @param notes documents lus en base
     * @return les DTO correspondants, dans l'ordre reçu
     */
    public List<NoteDto> versListeDto(List<Note> notes) {
        return notes.stream().map(this::versDto).toList();
    }
}
