package com.mr486.msrisque.client;

import com.mr486.commun.dto.NoteDto;
import com.mr486.msrisque.configuration.FeignSecurityConfiguration;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Client Feign de l'API des notes, résolu par son nom dans Eureka.
 *
 * <p>Comme {@link PatientClient}, il appelle {@code ms-notes} directement, en HTTP Basic.</p>
 *
 * <p><b>Exemple :</b> {@code getNotesByPatientId(TEST_BORDERLINE)} appelle
 * {@code GET /patients/2/notes} et retourne deux notes.</p>
 */
@FeignClient(name = "ms-notes", configuration = FeignSecurityConfiguration.class)
public interface NoteClient {

    /**
     * Lit l'historique des notes d'un patient.
     *
     * <p><b>Exemple :</b> un patient sans note retourne une liste vide.</p>
     *
     * @param id identifiant du patient
     * @return les notes du patient, de la plus récente à la plus ancienne
     */
    @GetMapping("/patients/{id}/notes")
    List<NoteDto> getNotesByPatientId(@PathVariable Long id);
}
