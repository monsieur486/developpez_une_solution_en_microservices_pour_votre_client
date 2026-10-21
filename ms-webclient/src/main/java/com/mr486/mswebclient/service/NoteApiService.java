package com.mr486.mswebclient.service;

import com.mr486.commun.dto.NoteDto;
import com.mr486.commun.dto.NoteForm;
import com.mr486.mswebclient.configuration.ConstantesWebclient;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

/**
 * Appels à l'API des notes, à travers la gateway.
 *
 * <p>Les erreurs HTTP sont traduites par {@link ClientPasserelle} : une panne de {@code ms-notes}
 * devient une {@code ServiceIndisponibleException} nommant ce service.</p>
 *
 * <p><b>Exemple :</b> {@code listeLesNotes(3L)} appelle {@code GET /ms-notes/patients/3/notes}.</p>
 */
@Service
@RequiredArgsConstructor
public class NoteApiService {
    private final ClientPasserelle clientPasserelle;

    /**
     * Récupère l'historique des notes d'un patient.
     *
     * <p><b>Exemple :</b> pour le patient 3 du jeu de démonstration, renvoie ses 2 notes, la plus
     * récente d'abord.</p>
     *
     * @param patientId identifiant du patient
     * @return ses notes, éventuellement aucune
     * @throws com.mr486.commun.exception.ServiceIndisponibleException si {@code ms-notes} est injoignable
     */
    public List<NoteDto> listeLesNotes(Long patientId) {
        return clientPasserelle.echange(
                HttpMethod.GET,
                ConstantesWebclient.CHEMIN_NOTES + patientId + "/notes",
                null,
                new ParameterizedTypeReference<List<NoteDto>>() {},
                ConstantesWebclient.SERVICE_NOTES);
    }

    /**
     * Ajoute une note au dossier d'un patient.
     *
     * <p><b>Exemple :</b> {@code ajouteUneNote(3L, form)} envoie {@code POST /ms-notes/patients/3/notes}
     * et renvoie la note datée par {@code ms-notes}.</p>
     *
     * @param patientId identifiant du patient
     * @param note      note saisie
     * @return la note enregistrée
     * @throws com.mr486.commun.exception.AppelRefuseException        si {@code ms-notes} refuse la note
     * @throws com.mr486.commun.exception.ServiceIndisponibleException si {@code ms-notes} est injoignable
     */
    public NoteDto ajouteUneNote(Long patientId, NoteForm note) {
        return clientPasserelle.echange(
                HttpMethod.POST,
                ConstantesWebclient.CHEMIN_NOTES + patientId + "/notes",
                note,
                new ParameterizedTypeReference<NoteDto>() {},
                ConstantesWebclient.SERVICE_NOTES);
    }
}
