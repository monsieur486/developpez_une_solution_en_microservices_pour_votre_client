package com.mr486.mswebclient.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;

import com.mr486.commun.dto.PatientForm;
import com.mr486.commun.exception.DuplicateException;
import com.mr486.mswebclient.configuration.ConstantesToast;
import com.mr486.mswebclient.service.PatientApiService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

@ExtendWith(MockitoExtension.class)
@DisplayName("Modification d'un patient depuis l'interface web")
class PatientDetailControllerTest {
    private static final Long ID = 42L;

    @Mock
    private PatientApiService patientApiService;

    @InjectMocks
    private PatientDetailController controleur;

    @Test
    @DisplayName("une modification en doublon réaffiche le formulaire avec un toast d'avertissement")
    void modificationEnDoublon_reafficheLeFormulaireAvecToast() {
        PatientForm patient = new PatientForm();
        doThrow(new DuplicateException("Ce patient existe déjà."))
                .when(patientApiService).metAJourLePatient(ID, patient);
        Model model = new ExtendedModelMap();

        String vue = controleur.updatePatient(
                ID, patient, new BeanPropertyBindingResult(patient, "patient"),
                model, new RedirectAttributesModelMap());

        assertThat(vue).isEqualTo("patient-update");
        assertThat(model.getAttribute("patientId")).isEqualTo(ID);
        assertThat(model.getAttribute(ConstantesToast.CLE_NIVEAU))
                .isEqualTo(ConstantesToast.NIVEAU_AVERTISSEMENT);
        assertThat(model.getAttribute(ConstantesToast.CLE_MESSAGE))
                .isEqualTo("Ce patient existe déjà.");
    }

    @Test
    @DisplayName("une modification aboutie redirige vers la fiche avec un toast de confirmation")
    void modificationAboutie_redirigeAvecToast() {
        PatientForm patient = new PatientForm();
        RedirectAttributes redirection = new RedirectAttributesModelMap();

        String vue = controleur.updatePatient(
                ID, patient, new BeanPropertyBindingResult(patient, "patient"),
                new ExtendedModelMap(), redirection);

        assertThat(vue).isEqualTo("redirect:/app/patients/" + ID);
        assertThat(redirection.getFlashAttributes().get(ConstantesToast.CLE_MESSAGE))
                .isEqualTo("Fiche patient mise à jour.");
    }
}
