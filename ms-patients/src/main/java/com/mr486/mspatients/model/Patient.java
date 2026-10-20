package com.mr486.mspatients.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Fiche d'identité d'un patient, persistée dans la table {@code patients}.
 *
 * <p>Le schéma est géré par Liquibase ; Hibernate se contente de le valider au démarrage
 * ({@code ddl-auto: validate}). L'adresse postale et le téléphone sont optionnels.</p>
 *
 * <p><b>Exemple :</b> le patient de démonstration {@code TestNone}, de genre {@code F}, est né le
 * 1966-12-31 et habite au 1 Brookside St.</p>
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@Entity
@Table(name = "patients")
public class Patient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    /** Genre : {@code M} ou {@code F}. */
    @Column(name = "gender", nullable = false)
    private String gender;

    @Column(name = "postal_address")
    private String postalAddress;

    @Column(name = "phone_number")
    private String phoneNumber;
}
