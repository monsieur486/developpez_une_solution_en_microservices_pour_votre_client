package com.mr486.mswebclient.controller;

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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/app")
@RequiredArgsConstructor
public class PatientsController {
    private static final String VUE_AJOUT = "patient-ajout";

    private final PatientApiService patientApiService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("patients", patientApiService.listeLesPatients());
        return "patients";
    }

    @GetMapping("/dashboard/ajout")
    public String showCreatePatientForm(Model model) {
        model.addAttribute("patient", new PatientForm());
        return VUE_AJOUT;
    }

    @PostMapping("/dashboard/ajout")
    public String ajoutPatientSubmit(
            @Valid @ModelAttribute("patient") PatientForm patient,
            BindingResult resultatDeLiaison,
            Model model,
            RedirectAttributes redirection) {
        if (resultatDeLiaison.hasErrors()) {
            return VUE_AJOUT;
        }
        try {
            patientApiService.creeLePatient(patient);
        } catch (DuplicateException ex) {
            model.addAttribute(ConstantesToast.CLE_NIVEAU, ConstantesToast.NIVEAU_AVERTISSEMENT);
            model.addAttribute(ConstantesToast.CLE_MESSAGE, ex.getMessage());
            return VUE_AJOUT;
        }
        redirection.addFlashAttribute(ConstantesToast.CLE_NIVEAU, ConstantesToast.NIVEAU_INFO);
        redirection.addFlashAttribute(ConstantesToast.CLE_MESSAGE, "Patient créé.");
        return "redirect:/app/dashboard";
    }
}
