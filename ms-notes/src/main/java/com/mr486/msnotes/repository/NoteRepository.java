package com.mr486.msnotes.repository;

import com.mr486.msnotes.model.Note;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Accès aux notes stockées dans MongoDB.
 *
 * <p><b>Exemple :</b> findByPatientIdOrderByCreatedDateDesc(2L) retourne les notes
 * du patient 2, de la plus récente à la plus ancienne.</p>
 */
@Repository
public interface NoteRepository extends MongoRepository<Note, UUID> {

    /**
     * Retourne les notes d'un patient, triées par date de création décroissante.
     *
     * <p><b>Exemple :</b> findByPatientIdOrderByCreatedDateDesc(2L) retourne la
     * liste des notes du patient 2 (vide si aucune).</p>
     *
     * @param patientId identifiant du patient
     * @return les notes du patient, de la plus récente à la plus ancienne
     */
    List<Note> findByPatientIdOrderByCreatedDateDesc(Long patientId);

    /**
     * Retourne une page des notes d'un patient (le tri vient du Pageable).
     *
     * <p><b>Exemple :</b> findByPatientId(2L, PageRequest.of(0, 5, tri)) retourne
     * les 5 notes les plus récentes du patient 2.</p>
     *
     * @param patientId identifiant du patient
     * @param pageable  page demandée et tri à appliquer
     * @return la page de notes correspondante
     */
    Page<Note> findByPatientId(Long patientId, Pageable pageable);
}
