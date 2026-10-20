package com.mr486.msrisque.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.mr486.commun.exception.AppelRefuseException;
import com.mr486.commun.exception.ServiceIndisponibleException;
import com.mr486.msrisque.client.NoteClient;
import feign.FeignException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("Récupération des notes d'un patient")
class NotesServiceTest {

    private static final Long PATIENT_ID = 2L;

    @Mock
    private NoteClient noteClient;

    private NotesService notesService;

    @BeforeEach
    void initialiseLeService() {
        notesService = new NotesService(noteClient);
    }

    @Test
    @DisplayName("un patient sans note retourne une liste vide sans erreur")
    void aucuneNote_retourneListeVide() {
        when(noteClient.getNotesByPatientId(PATIENT_ID)).thenReturn(List.of());

        assertThat(notesService.getNotesByPatientId(PATIENT_ID)).isEmpty();
    }

    @Test
    @DisplayName("une réponse sans corps retourne une liste vide")
    void reponseNulle_retourneListeVide() {
        when(noteClient.getNotesByPatientId(PATIENT_ID)).thenReturn(null);

        assertThat(notesService.getNotesByPatientId(PATIENT_ID)).isEmpty();
    }

    @Test
    @DisplayName("un service des notes injoignable devient une indisponibilité de service")
    void serviceInjoignable_leveServiceIndisponible() {
        when(noteClient.getNotesByPatientId(PATIENT_ID))
                .thenThrow(mock(FeignException.ServiceUnavailable.class));

        assertThatThrownBy(() -> notesService.getNotesByPatientId(PATIENT_ID))
                .isInstanceOf(ServiceIndisponibleException.class)
                .hasMessageContaining("ms-notes");
    }

    @Test
    @DisplayName("une ressource absente côté service des notes retourne une liste vide")
    void ressourceAbsente_retourneListeVide() {
        when(noteClient.getNotesByPatientId(PATIENT_ID))
                .thenThrow(mock(FeignException.NotFound.class));

        assertThat(notesService.getNotesByPatientId(PATIENT_ID)).isEmpty();
    }

    // Un 401 signale des identifiants techniques erronés : réessayer n'y changerait rien.
    @Test
    @DisplayName("un refus du service (401) devient un appel refusé, pas une indisponibilité")
    void refusDuService_leveAppelRefuse() {
        FeignException.Unauthorized refus = mock(FeignException.Unauthorized.class);
        when(refus.status()).thenReturn(401);
        when(noteClient.getNotesByPatientId(PATIENT_ID)).thenThrow(refus);

        assertThatThrownBy(() -> notesService.getNotesByPatientId(PATIENT_ID))
                .isInstanceOf(AppelRefuseException.class)
                .hasMessage("Le service ms-notes a refusé la requête (statut 401).");
    }
}
