package com.mr486.mswebclient.service;

import com.mr486.commun.dto.NoteDto;
import com.mr486.mswebclient.configuration.ConstantesWebclient;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NoteApiService {
    private final ClientPasserelle clientPasserelle;

    public List<NoteDto> listeLesNotes(Long patientId) {
        return clientPasserelle.echange(
                HttpMethod.GET,
                ConstantesWebclient.CHEMIN_NOTES + patientId + "/notes",
                null,
                new ParameterizedTypeReference<List<NoteDto>>() {},
                ConstantesWebclient.SERVICE_NOTES);
    }

    public NoteDto ajouteUneNote(Long patientId, NoteDto note) {
        return clientPasserelle.echange(
                HttpMethod.POST,
                ConstantesWebclient.CHEMIN_NOTES + patientId + "/notes",
                note,
                new ParameterizedTypeReference<NoteDto>() {},
                ConstantesWebclient.SERVICE_NOTES);
    }
}
