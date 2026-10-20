package com.mr486.mspatients.repository;

import com.mr486.mspatients.model.Patient;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Accès aux fiches patients en base PostgreSQL.
 *
 * <p>Les opérations courantes (lecture, enregistrement) sont fournies par Spring Data ; seule la
 * recherche de doublon est déclarée ici.</p>
 *
 * <p><b>Exemple :</b> {@code patientRepository.findById(id)} renvoie {@code Optional.empty()}
 * pour un identifiant inconnu.</p>
 */
public interface PatientRepository extends JpaRepository<Patient, Long> {

    /**
     * Indique si un patient de même identité est déjà enregistré.
     *
     * <p><b>Exemple :</b> avec le jeu de démonstration, {@code ("TestNone", "Test", 1966-12-31, "F")}
     * renvoie {@code true}.</p>
     *
     * @param lastName  nom de famille
     * @param firstName prénom
     * @param birthDate date de naissance
     * @param gender    genre ({@code M} ou {@code F})
     * @return {@code true} si un patient porte exactement cette identité
     */
    boolean existsByLastNameAndFirstNameAndBirthDateAndGender(
            String lastName, String firstName, LocalDate birthDate, String gender);
}
