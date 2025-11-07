package com.mr486.msrisque.service;

import com.mr486.msrisque.client.PatientClient;
import com.mr486.msrisque.dto.Patient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PatientService {

  private final PatientClient patientClient;

  public Patient getPatientById(Long id) {
    return patientClient.findById(id);
  }

}
