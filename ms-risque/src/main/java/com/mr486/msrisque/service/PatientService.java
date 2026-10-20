package com.mr486.msrisque.service;

import com.mr486.commun.dto.PatientDto;
import com.mr486.commun.exception.AppelRefuseException;
import com.mr486.commun.exception.ServiceIndisponibleException;
import com.mr486.msrisque.client.PatientClient;
import com.mr486.msrisque.configuration.ConstantesRisque;
import com.mr486.msrisque.exception.PatientIntrouvableException;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Récupère la fiche d'un patient auprès de {@code ms-patients}.
 *
 * <p>Il traduit les erreurs Feign en exceptions métier : 404 → patient introuvable, autre 4xx →
 * appel refusé, tout le reste (5xx, délai dépassé, service absent) → service indisponible.</p>
 *
 * <p><b>Exemple :</b> {@code getPatientById(TEST_NONE)} retourne la fiche de {@code TestNone},
 * née le 1966-12-31.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PatientService {
    private final PatientClient patientClient;

    /**
     * Retourne la fiche d'un patient.
     *
     * <p><b>Exemple :</b> un identifiant inconnu de {@code ms-patients} lève
     * {@code PatientIntrouvableException}.</p>
     *
     * @param id identifiant du patient
     * @return la fiche du patient, avec sa date de naissance et son genre
     * @throws PatientIntrouvableException si {@code ms-patients} répond 404
     * @throws AppelRefuseException si {@code ms-patients} refuse l'appel (autre 4xx)
     * @throws ServiceIndisponibleException si {@code ms-patients} est injoignable ou en erreur
     */
    public PatientDto getPatientById(Long id) {
        try {
            return patientClient.findById(id);
        } catch (FeignException.NotFound ex) {
            log.warn("patient {} introuvable auprès du service des patients", id);
            throw new PatientIntrouvableException(id, ex);
        } catch (FeignException.FeignClientException ex) {
            throw new AppelRefuseException(ConstantesRisque.SERVICE_PATIENTS, ex.status(), ex);
        } catch (FeignException ex) {
            throw new ServiceIndisponibleException(ConstantesRisque.SERVICE_PATIENTS, ex);
        }
    }
}
