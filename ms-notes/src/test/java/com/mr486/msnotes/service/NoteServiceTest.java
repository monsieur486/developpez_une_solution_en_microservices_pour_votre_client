package com.mr486.msnotes.service;

import com.mr486.msnotes.dto.NoteDto;
import com.mr486.msnotes.model.Note;
import com.mr486.msnotes.repository.NoteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NoteServiceTest {

    @Mock
    private NoteRepository noteRepository;

    @InjectMocks
    private NoteService noteService;

    @Test
    void findByPatientId_retourneLesNotesDuPatient() {
        when(noteRepository.findByPatientIdOrderByCreatedDateDesc(2L))
                .thenReturn(List.of(new Note(), new Note()));

        assertThat(noteService.findByPatientId(2L)).hasSize(2);
    }

    @Test
    void findByPatientId_pagine_retourneLes5DernieresNotesTrieesParDateDecroissante() {
        when(noteRepository.findByPatientId(eq(2L), any(Pageable.class)))
                .thenAnswer(inv -> {
                    Pageable demande = inv.getArgument(1);
                    assertThat(demande.getPageSize()).isEqualTo(5);
                    assertThat(demande.getSort().getOrderFor("createdDate").getDirection())
                            .isEqualTo(Sort.Direction.DESC);
                    return new PageImpl<>(List.of(new Note()));
                });

        assertThat(noteService.findByPatientId(2L, 0).getContent()).hasSize(1);
    }

    @Test
    void save_construitUneNoteDateePuisLaPersiste() {
        when(noteRepository.save(any(Note.class))).thenAnswer(inv -> inv.getArgument(0));

        Note enregistree = noteService.save(2L, NoteDto.builder().content("RAS").build());

        assertThat(enregistree.getPatientId()).isEqualTo(2L);
        assertThat(enregistree.getContent()).isEqualTo("RAS");
        assertThat(enregistree.getCreatedDate()).isNotNull();
    }
}
