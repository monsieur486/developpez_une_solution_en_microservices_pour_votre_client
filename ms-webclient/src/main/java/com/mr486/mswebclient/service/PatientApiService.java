package com.mr486.mswebclient.service;

import com.mr486.commun.dto.PatientDto;
import com.mr486.commun.dto.PatientForm;
import com.mr486.mswebclient.configuration.ConstantesWebclient;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PatientApiService {
    private final ClientPasserelle clientPasserelle;

    public List<PatientDto> listeLesPatients() {
        return clientPasserelle.echange(
                HttpMethod.GET,
                ConstantesWebclient.CHEMIN_PATIENTS,
                null,
                new ParameterizedTypeReference<List<PatientDto>>() {},
                ConstantesWebclient.SERVICE_PATIENTS);
    }

    public PatientDto recupereLePatient(Long id) {
        return clientPasserelle.echange(
                HttpMethod.GET,
                ConstantesWebclient.CHEMIN_PATIENTS + "/" + id,
                null,
                new ParameterizedTypeReference<PatientDto>() {},
                ConstantesWebclient.SERVICE_PATIENTS);
    }

    public PatientDto creeLePatient(PatientForm formulaire) {
        return clientPasserelle.echange(
                HttpMethod.POST,
                ConstantesWebclient.CHEMIN_PATIENTS,
                formulaire,
                new ParameterizedTypeReference<PatientDto>() {},
                ConstantesWebclient.SERVICE_PATIENTS);
    }

    public PatientDto metAJourLePatient(Long id, PatientForm formulaire) {
        return clientPasserelle.echange(
                HttpMethod.PUT,
                ConstantesWebclient.CHEMIN_PATIENTS + "/" + id,
                formulaire,
                new ParameterizedTypeReference<PatientDto>() {},
                ConstantesWebclient.SERVICE_PATIENTS);
    }
}
