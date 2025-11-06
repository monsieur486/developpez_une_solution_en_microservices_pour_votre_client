package com.mr486.mspatients.service;

import com.mr486.mspatients.dto.PatientForm;
import com.mr486.mspatients.exeption.NotFoundException;
import com.mr486.mspatients.model.Patient;
import com.mr486.mspatients.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PatientService {

  private final PatientRepository patientRepository;

  public List<Patient> findAll() {
    return patientRepository.findAll();
  }

  public Patient findById(Long id) {
    return patientRepository
            .findById(id)
            .orElseThrow(() -> new NotFoundException("Aucun patient trouvé avec l'id: " + id));
  }

  public Patient savePatient(PatientForm patientForm) {
    Patient patient = fromPatientForm(patientForm);
    return patientRepository.save(patient);
  }

  public Patient updatePatient(Long id, PatientForm patientForm) {
    Patient patient = patientRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Aucun patient trouvé avec l'id: " + id));
    patient.setFirstName(patientForm.getFirstName());
    patient.setLastName(patientForm.getLastName());
    patient.setBirthDate(patientForm.getBirthDate());
    patient.setGender(patientForm.getGender());
    patient.setPostalAddress(patientForm.getPostalAddress());
    patient.setPhoneNumber(patientForm.getPhoneNumber());
    return patientRepository.save(patient);
  }

  private Patient fromPatientForm(PatientForm patientForm) {
    Patient patient = new Patient();
    patient.setFirstName(patientForm.getFirstName());
    patient.setLastName(patientForm.getLastName());
    patient.setBirthDate(patientForm.getBirthDate());
    patient.setGender(patientForm.getGender());
    patient.setPostalAddress(patientForm.getPostalAddress());
    patient.setPhoneNumber(patientForm.getPhoneNumber());
    return patient;
  }
}
