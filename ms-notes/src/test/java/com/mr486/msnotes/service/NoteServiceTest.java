package com.mr486.msnotes.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mr486.commun.dto.NoteForm;
import com.mr486.msnotes.model.Note;
import com.mr486.msnotes.repository.NoteRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("Gestion des notes")
class NoteServiceTest {
    private static final Long PATIENT_RISQUE_LIMITE = 2L;

    private static final Instant MAINTENANT = Instant.parse("2026-10-20T16:13:39Z");

    @Mock
    private NoteRepository noteRepository;

    private NoteService noteService;

    @BeforeEach
    void initialiseLeService() {
        // Horloge figée : la date attendue est connue d'avance, le test ne dépend pas de l'heure.
        noteService = new NoteService(noteRepository, Clock.fixed(MAINTENANT, ZoneOffset.UTC));
    }

    @Test
    @DisplayName("une note est enregistrée pour le patient de l'URL, datée par l'horloge, texte intact")
    void save_enregistreLaNoteDuPatientDateeParLHorloge() {
        when(noteRepository.save(any(Note.class))).thenAnswer(appel -> appel.getArgument(0));

        noteService.save(PATIENT_RISQUE_LIMITE,
                NoteForm.builder().content("Audition ANORMALE, Réaction aux médicaments").build());

        ArgumentCaptor<Note> enregistree = ArgumentCaptor.forClass(Note.class);
        verify(noteRepository).save(enregistree.capture());
        // La casse est conservée : l'évaluation du risque recherchera les termes dans ce texte.
        assertThat(enregistree.getValue())
                .extracting(Note::getId, Note::getPatientId, Note::getContent, Note::getCreatedDate)
                .containsExactly(null, PATIENT_RISQUE_LIMITE,
                        "Audition ANORMALE, Réaction aux médicaments", MAINTENANT);
    }
}
