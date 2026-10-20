package com.mr486.msrisque.service;

import com.mr486.commun.dto.NoteDto;
import com.mr486.commun.exception.AppelRefuseException;
import com.mr486.commun.exception.ServiceIndisponibleException;
import com.mr486.msrisque.client.NoteClient;
import com.mr486.msrisque.configuration.ConstantesRisque;
import feign.FeignException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Récupère les notes d'un patient auprès de {@code ms-notes}.
 *
 * <p>Un patient sans note n'est pas une erreur : une réponse vide ou un 404 donnent une liste vide.
 * Un autre 4xx devient un appel refusé, tout le reste une indisponibilité de service.</p>
 *
 * <p><b>Exemple :</b> {@code getNotesByPatientId(TEST_NONE)} retourne une note, qui cite « Poids ».</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotesService {

    private final NoteClient noteClient;

    /**
     * Retourne les notes d'un patient.
     *
     * <p><b>Exemple :</b> un patient sans historique retourne une liste vide, jamais {@code null}.</p>
     *
     * @param patientId identifiant du patient
     * @return les notes du patient, éventuellement vides
     * @throws AppelRefuseException si {@code ms-notes} refuse l'appel (4xx autre que 404)
     * @throws ServiceIndisponibleException si {@code ms-notes} est injoignable ou en erreur
     */
    public List<NoteDto> getNotesByPatientId(Long patientId) {
        try {
            List<NoteDto> notes = noteClient.getNotesByPatientId(patientId);
            if (notes == null) {
                log.warn("réponse sans corps du service des notes pour le patient {}", patientId);
                return List.of();
            }
            log.debug("{} note(s) rassemblée(s) pour le patient {}", notes.size(), patientId);
            return notes;
        } catch (FeignException.NotFound ex) {
            log.warn("aucune note retournée pour le patient {}", patientId);
            return List.of();
        } catch (FeignException.FeignClientException ex) {
            throw new AppelRefuseException(ConstantesRisque.SERVICE_NOTES, ex.status(), ex);
        } catch (FeignException ex) {
            throw new ServiceIndisponibleException(ConstantesRisque.SERVICE_NOTES, ex);
        }
    }
}
