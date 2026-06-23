package com.mr486.mspatients.service;

import com.mr486.mspatients.dto.PatientForm;
import com.mr486.mspatients.exception.NotFoundException;
import com.mr486.mspatients.model.Patient;
import com.mr486.mspatients.repository.PatientRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private PatientService patientService;

    // Construit un formulaire patient valide.
    private PatientForm formulaire() {
        return PatientForm.builder()
                .firstName("John")
                .lastName("Doe")
                .birthDate(LocalDate.of(1990, 1, 1))
                .gender("M")
                .postalAddress("1 rue des Lilas")
                .phoneNumber("0102030405")
                .build();
    }

    @Test
    void findAll_retourneTousLesPatients() {
        when(patientRepository.findAll()).thenReturn(List.of(new Patient(), new Patient()));

        assertThat(patientService.findAll()).hasSize(2);
    }

    @Test
    void findById_retourneLePatientQuandIlExiste() {
        Patient patient = new Patient();
        patient.setId(7L);
        when(patientRepository.findById(7L)).thenReturn(Optional.of(patient));

        assertThat(patientService.findById(7L)).isSameAs(patient);
    }

    @Test
    void findById_leveNotFoundQuandAbsent() {
        when(patientRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> patientService.findById(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void savePatient_recopieLeFormulaireEtPersiste() {
        when(patientRepository.save(any(Patient.class))).thenAnswer(inv -> inv.getArgument(0));

        Patient enregistre = patientService.savePatient(formulaire());

        assertThat(enregistre.getFirstName()).isEqualTo("John");
        assertThat(enregistre.getGender()).isEqualTo("M");
        assertThat(enregistre.getPhoneNumber()).isEqualTo("0102030405");
    }

    @Test
    void updatePatient_metAJourQuandLePatientExiste() {
        Patient existant = new Patient();
        existant.setId(7L);
        existant.setFirstName("Ancien");
        when(patientRepository.findById(7L)).thenReturn(Optional.of(existant));
        when(patientRepository.save(any(Patient.class))).thenAnswer(inv -> inv.getArgument(0));

        Patient maj = patientService.updatePatient(7L, formulaire());

        assertThat(maj.getFirstName()).isEqualTo("John");
    }

    @Test
    void updatePatient_leveNotFoundQuandAbsent() {
        when(patientRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> patientService.updatePatient(999L, formulaire()))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("999");
    }
}
