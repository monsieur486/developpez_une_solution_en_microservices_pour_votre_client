package com.mr486.mswebclient.controller;

import com.mr486.commun.dto.PatientDto;
import com.mr486.commun.dto.PatientForm;
import com.mr486.commun.exception.DuplicateException;
import com.mr486.mswebclient.configuration.ConstantesToast;
import com.mr486.mswebclient.service.PatientApiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/app")
@RequiredArgsConstructor
public class PatientDetailController {
    private static final String VUE_MODIFICATION = "patient-update";

    private static final String ATTRIBUT_ID = "patientId";

    private final PatientApiService patientApiService;

    @GetMapping("/patients/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("patient", patientApiService.recupereLePatient(id));
        model.addAttribute(ATTRIBUT_ID, id);
        return "patient-detail";
    }

    @GetMapping("/patients/{id}/update")
    public String updatePatientForm(@PathVariable Long id, Model model) {
        model.addAttribute("patient", versFormulaire(patientApiService.recupereLePatient(id)));
        model.addAttribute(ATTRIBUT_ID, id);
        return VUE_MODIFICATION;
    }

    @PostMapping("/patients/{id}/update")
    public String updatePatient(
            @PathVariable Long id,
            @Valid @ModelAttribute("patient") PatientForm patient,
            BindingResult resultatDeLiaison,
            Model model,
            RedirectAttributes redirection) {
        if (resultatDeLiaison.hasErrors()) {
            model.addAttribute(ATTRIBUT_ID, id);
            return VUE_MODIFICATION;
        }
        try {
            patientApiService.metAJourLePatient(id, patient);
        } catch (DuplicateException ex) {
            model.addAttribute(ATTRIBUT_ID, id);
            model.addAttribute(ConstantesToast.CLE_NIVEAU, ConstantesToast.NIVEAU_AVERTISSEMENT);
            model.addAttribute(ConstantesToast.CLE_MESSAGE, ex.getMessage());
            return VUE_MODIFICATION;
        }
        redirection.addFlashAttribute(ConstantesToast.CLE_NIVEAU, ConstantesToast.NIVEAU_INFO);
        redirection.addFlashAttribute(ConstantesToast.CLE_MESSAGE, "Fiche patient mise à jour.");
        return "redirect:/app/patients/" + id;
    }

    private PatientForm versFormulaire(PatientDto patient) {
        return PatientForm.builder()
                .firstName(patient.getFirstName())
                .lastName(patient.getLastName())
                .birthDate(patient.getBirthDate())
                .gender(patient.getGender())
                .postalAddress(patient.getPostalAddress())
                .phoneNumber(patient.getPhoneNumber())
                .build();
    }
}
