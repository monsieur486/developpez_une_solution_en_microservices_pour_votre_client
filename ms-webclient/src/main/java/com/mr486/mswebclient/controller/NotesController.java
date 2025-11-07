package com.mr486.mswebclient.controller;

import com.mr486.mswebclient.dto.ErrorMessage;
import com.mr486.mswebclient.dto.Note;
import com.mr486.mswebclient.tools.ErrorResponseTools;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Controller
@RequestMapping("/app")
public class NotesController {

  private final RestTemplate restTemplate;
  private final String gatewayBase;
  private final ErrorResponseTools errorResponseTools;

  private final String microserviceName = "ms-notes";

  public NotesController(RestTemplate restTemplate,
                         @Value("${app.gateway.base-url}") String gatewayBase, ErrorResponseTools errorResponseTools) {
    this.restTemplate = restTemplate;
    this.gatewayBase = gatewayBase;
    this.errorResponseTools = errorResponseTools;
  }

  @GetMapping("/dashboard/{patientId}/notes")
  public String getNotes(Model model, @PathVariable Long patientId) {
    List<Note> notes;
    try {
      ResponseEntity<List<Note>> response = restTemplate.exchange(
              gatewayBase + "/ms-notes/patients/" + patientId + "/notes",
              HttpMethod.GET,
              null,
              new ParameterizedTypeReference<List<Note>>() {
              }
      );
      notes = response.getBody();
      model.addAttribute("notes", notes);
      model.addAttribute("patientId", patientId);
    } catch (Exception e) {
      ErrorMessage errorMessage = errorResponseTools.getErrorMessage(e.getMessage(), microserviceName);
      model.addAttribute("errorMessage", errorMessage);
      model.addAttribute("patientId", patientId);
    }
    return "notes";
  }

  @GetMapping("/dashboard/{patientId}/notes/ajout")
  public String showCreateNoteForm(@PathVariable Long patientId, Model model) {
    model.addAttribute("patientId", patientId);
    model.addAttribute("note", new Note());
    return "note-ajout";
  }

  @PostMapping("/dashboard/{patientId}/notes")
  public String ajoutNotePost(@PathVariable Long patientId, Note note, Model model) {
    HttpEntity<Note> requestEntity = new HttpEntity<>(note);
    try {
      ResponseEntity<Note> scoreResponse = restTemplate.exchange(
              gatewayBase + "/ms-notes/patients/" + patientId + "/notes",
              HttpMethod.POST,
              requestEntity,
              new ParameterizedTypeReference<Note>() {
              }
      );
      return "redirect:/app/dashboard/{patientId}/notes";
    } catch (Exception ex) {
      ErrorMessage errorMessage = errorResponseTools.getErrorMessage(ex.getMessage(), microserviceName);
      model.addAttribute("patientId", patientId);
      model.addAttribute("note", note);
      model.addAttribute("errorMessage", errorMessage);
      return "note-ajout";
    }
  }
}
