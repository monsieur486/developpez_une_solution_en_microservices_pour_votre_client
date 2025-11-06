package com.mr486.mswebclient.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientForm {
  private String firstName;
  private String lastName;
  @DateTimeFormat(pattern = "yyyy-MM-dd")
  private LocalDate birthDate;
  private String gender;
  private String postalAddress;
  private String phoneNumber;
}
