package com.mr486.mspatients.controller;

import com.mr486.mspatients.dto.PageReponse;
import com.mr486.mspatients.dto.PatientForm;
import com.mr486.mspatients.model.Patient;
import com.mr486.mspatients.service.PatientService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PatientControllerTest {

    @Mock
    private PatientService patientService;

    @InjectMocks
    private PatientController patientController;

    // Construit un formulaire patient valide.
    private PatientForm formulaire() {
        return PatientForm.builder()
                .firstName("John")
                .lastName("Doe")
                .birthDate(LocalDate.of(1990, 1, 1))
                .gender("M")
                .build();
    }

    @Test
    void getPatients_retourne200EtLaPage() {
        when(patientService.findAll(0)).thenReturn(new PageImpl<>(List.of(new Patient())));

        ResponseEntity<PageReponse<Patient>> reponse = patientController.getPatients(0);

        assertThat(reponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(reponse.getBody()).isNotNull();
        assertThat(reponse.getBody().getContent()).hasSize(1);
        assertThat(reponse.getBody().getTotalElements()).isEqualTo(1);
    }

    @Test
    void getPatient_retourne200EtLePatient() {
        Patient patient = new Patient();
        patient.setId(7L);
        when(patientService.findById(7L)).thenReturn(patient);

        ResponseEntity<Patient> reponse = patientController.getPatient(7L);

        assertThat(reponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(reponse.getBody()).isSameAs(patient);
    }

    @Test
    void createPatient_retourne201() {
        Patient cree = new Patient();
        when(patientService.savePatient(any(PatientForm.class))).thenReturn(cree);

        ResponseEntity<Patient> reponse = patientController.createPatient(formulaire());

        assertThat(reponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(reponse.getBody()).isSameAs(cree);
    }

    @Test
    void update_retourne200() {
        Patient maj = new Patient();
        when(patientService.updatePatient(eq(7L), any(PatientForm.class))).thenReturn(maj);

        ResponseEntity<Patient> reponse = patientController.update(7L, formulaire());

        assertThat(reponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(reponse.getBody()).isSameAs(maj);
    }
}
