package com.mr486.mswebclient.service;

import com.mr486.mswebclient.dto.Patient;
import com.mr486.mswebclient.dto.PatientForm;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PatientServiceTest {

    private static final String BASE = "http://gateway";

    private final RestTemplate restTemplate = mock(RestTemplate.class);
    private final PatientService service = new PatientService(restTemplate, BASE);

    @Test
    void getPatients_appelleLaPasserelleEtRetourneLaListe() {
        List<Patient> attendus = List.of(new Patient());
        when(restTemplate.exchange(eq(BASE + "/ms-patients/patients"), eq(HttpMethod.GET),
                isNull(), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(attendus));

        List<Patient> patients = service.getPatients();

        assertThat(patients).isEqualTo(attendus);
    }

    @Test
    void getPatientById_appelleLaPasserelleEtRetourneLePatient() {
        Patient attendu = new Patient();
        attendu.setId(7L);
        when(restTemplate.exchange(eq(BASE + "/ms-patients/patients/7"), eq(HttpMethod.GET),
                isNull(), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(attendu));

        Patient patient = service.getPatientById(7L);

        assertThat(patient.getId()).isEqualTo(7L);
    }

    @Test
    void createPatient_envoieLeFormulaireEnPost() {
        PatientForm form = new PatientForm();
        when(restTemplate.exchange(eq(BASE + "/ms-patients/patients"), eq(HttpMethod.POST),
                any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok().build());

        service.createPatient(form);

        verify(restTemplate).exchange(eq(BASE + "/ms-patients/patients"), eq(HttpMethod.POST),
                any(HttpEntity.class), any(ParameterizedTypeReference.class));
    }

    @Test
    void updatePatient_envoieLeFormulaireEnPut() {
        PatientForm form = new PatientForm();
        when(restTemplate.exchange(eq(BASE + "/ms-patients/patients/7"), eq(HttpMethod.PUT),
                any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok().build());

        service.updatePatient(7L, form);

        verify(restTemplate).exchange(eq(BASE + "/ms-patients/patients/7"), eq(HttpMethod.PUT),
                any(HttpEntity.class), any(ParameterizedTypeReference.class));
    }
}
