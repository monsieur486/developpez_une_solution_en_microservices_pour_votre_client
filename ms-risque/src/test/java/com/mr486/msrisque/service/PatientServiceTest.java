package com.mr486.msrisque.service;

import com.mr486.msrisque.client.PatientClient;
import com.mr486.msrisque.dto.Patient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock
    private PatientClient patientClient;

    @InjectMocks
    private PatientService patientService;

    @Test
    void getPatientById_delegueAuClient() {
        Patient patient = Patient.builder().id(7L).build();
        when(patientClient.findById(7L)).thenReturn(patient);

        assertThat(patientService.getPatientById(7L)).isSameAs(patient);
    }
}
