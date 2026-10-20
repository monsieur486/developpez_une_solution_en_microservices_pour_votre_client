package com.mr486.msnotes.mapper;

import com.mr486.commun.dto.NoteDto;
import com.mr486.msnotes.model.Note;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class NoteMapper {
    public NoteDto versDto(Note note) {
        return NoteDto.builder()
                .content(note.getContent())
                .build();
    }

    public List<NoteDto> versListeDto(List<Note> notes) {
        return notes.stream().map(this::versDto).toList();
    }
}
