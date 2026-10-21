package com.mr486.msnotes.repository;

import com.mr486.msnotes.model.Note;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Accès aux notes des praticiens dans la collection MongoDB {@code notes}.
 *
 * <p>L'enregistrement est fourni par Spring Data ; seule la lecture de l'historique d'un patient
 * est déclarée ici.</p>
 *
 * <p><b>Exemple :</b> {@code noteRepository.save(note)} attribue un identifiant MongoDB à la note.</p>
 */
@Repository
public interface NoteRepository extends MongoRepository<Note, String> {
    /**
     * Renvoie l'historique d'un patient, de la note la plus récente à la plus ancienne.
     *
     * <p><b>Exemple :</b> avec le jeu de démonstration, le patient 4 ({@code TestEarlyOnset}) a
     * 4 notes ; un patient sans note donne une liste vide.</p>
     *
     * @param patientId identifiant du patient dans {@code ms-patients}
     * @return les notes du patient, triées par date décroissante
     */
    List<Note> findByPatientIdOrderByCreatedDateDesc(Long patientId);
}
