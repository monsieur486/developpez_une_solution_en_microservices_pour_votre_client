package com.mr486.mspatients.repository;

import com.mr486.mspatients.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Accès aux patients en base (CRUD fourni par Spring Data JPA).
 *
 * <p><b>Exemple :</b> patientRepository.findById(7L) retourne un Optional du
 * patient 7.</p>
 */
@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {
}
