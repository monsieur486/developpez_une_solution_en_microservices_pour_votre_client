package com.mr486.msrisque.service;

import com.mr486.msrisque.dto.Note;
import com.mr486.msrisque.dto.Patient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EvaluationService {

  private final PatientService patientService;
  private final NotesService notesService;

  private static int getDeclencheursCount(List<Note> notes) {
    List<String> termesDeclencheurs = List.of(
            "hémoglobine A1C",
            "microalbumine",
            "taille",
            "poids",
            "fumeur",
            "anormal",
            "cholestérol",
            "vertige",
            "rechute",
            "réaction",
            "anticorps"
    );

    // Compter le nombre de déclencheurs présents dans les notes
    int declencheursCount = 0;
    for (Note note : notes) {
      String contenu = note.getContent().toLowerCase();
      for (String terme : termesDeclencheurs) {
        if (contenu.contains(terme)) {
          declencheursCount++;
        }
      }
    }
    return declencheursCount;
  }

  public String evaluationDuRisque(Long patientId) {
    Patient patient = patientService.getPatientById(patientId);
    List<Note> notes = notesService.getNotesByPatientId(patientId);
    String genre = patient.getGender();
    int age = calculAge(patient.getBirthDate());
    int declencheursCount = getDeclencheursCount(notes);

    // Détermination du niveau de risque
    String niveauRisque;
    if (declencheursCount == 0) {
      niveauRisque = "None";
    } else if (declencheursCount >= 2 && declencheursCount <= 5 && age > 30) {
      niveauRisque = "Borderline";
    } else if (
            (age < 30 && "M".equalsIgnoreCase(genre) && declencheursCount == 3) ||
                    (age < 30 && "F".equalsIgnoreCase(genre) && declencheursCount == 4) ||
                    (age > 30 && (declencheursCount == 6 || declencheursCount == 7))
    ) {
      niveauRisque = "In Danger";
    } else if (
            (age < 30 && "M".equalsIgnoreCase(genre) && declencheursCount >= 5) ||
                    (age < 30 && "F".equalsIgnoreCase(genre) && declencheursCount >= 7) ||
                    (age > 30 && declencheursCount >= 8)
    ) {
      niveauRisque = "Early onset";
    } else {
      niveauRisque = "None";
    }

    return niveauRisque;
  }

  private int calculAge(LocalDate birthDate) {
    return LocalDate.now().getYear() - birthDate.getYear();
  }


}
