package com.mr486.mspatients;

import com.mr486.mspatients.dto.PatientForm;
import com.mr486.mspatients.model.Patient;
import com.mr486.mspatients.service.PatientService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@SpringBootTest
@Transactional
class PatientServiceDuplicateTests {

  @Autowired
  private PatientService patientService;

  @Test
  void updateToExistingIdentity_shouldThrowDuplicateException() {
    // 1) Créer un patient distinct
    PatientForm create = PatientForm.builder()
            .firstName("John ") // espaces fin
            .lastName("  Doe")  // espaces début
            .birthDate(LocalDate.of(1990, 1, 1))
            .gender("m")
            .postalAddress("Addr")
            .phoneNumber("000")
            .build();
    Patient created = patientService.savePatient(create);

  }
}

