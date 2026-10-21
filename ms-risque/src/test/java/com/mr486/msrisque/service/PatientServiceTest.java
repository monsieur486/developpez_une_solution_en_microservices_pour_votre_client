package com.mr486.msrisque.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.mr486.commun.exception.AppelRefuseException;
import com.mr486.commun.exception.ServiceIndisponibleException;
import com.mr486.msrisque.client.PatientClient;
import com.mr486.msrisque.exception.PatientIntrouvableException;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("Récupération d'une fiche patient")
class PatientServiceTest {
    private static final Long PATIENT_ID = 42L;

    @Mock
    private PatientClient patientClient;

    private PatientService patientService;

    @BeforeEach
    void initialiseLeService() {
        patientService = new PatientService(patientClient);
    }

    @Test
    @DisplayName("une fiche absente devient une ressource introuvable")
    void ficheAbsente_levePatientIntrouvable() {
        when(patientClient.findById(PATIENT_ID)).thenThrow(mock(FeignException.NotFound.class));

        assertThatThrownBy(() -> patientService.getPatientById(PATIENT_ID))
                .isInstanceOf(PatientIntrouvableException.class)
                .hasMessageContaining("42");
    }

    @Test
    @DisplayName("un service des patients injoignable devient une indisponibilité de service")
    void serviceInjoignable_leveServiceIndisponible() {
        when(patientClient.findById(PATIENT_ID))
                .thenThrow(mock(FeignException.ServiceUnavailable.class));

        assertThatThrownBy(() -> patientService.getPatientById(PATIENT_ID))
                .isInstanceOf(ServiceIndisponibleException.class)
                .hasMessageContaining("ms-patients");
    }

    // Un 401 signale des identifiants techniques erronés : réessayer n'y changerait rien.
    @Test
    @DisplayName("un refus du service (401) devient un appel refusé, pas une indisponibilité")
    void refusDuService_leveAppelRefuse() {
        FeignException.Unauthorized refus = mock(FeignException.Unauthorized.class);
        when(refus.status()).thenReturn(401);
        when(patientClient.findById(PATIENT_ID)).thenThrow(refus);

        assertThatThrownBy(() -> patientService.getPatientById(PATIENT_ID))
                .isInstanceOf(AppelRefuseException.class)
                .hasMessage("Le service ms-patients a refusé la requête (statut 401).");
    }
}
