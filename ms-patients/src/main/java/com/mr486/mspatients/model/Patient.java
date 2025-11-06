package com.mr486.mspatients.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity
@Table(name = "patients")
public class Patient {
  @Id()
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(name = "first_name", nullable = false)
  private String firstName;
  @Column(name = "last_name", nullable = false)
  private String lastName;
  @Column(name = "birth_date", nullable = false)
  private LocalDate birthDate;
  @Column(name = "gender", nullable = false)
  private String gender;
  @Column(name = "postal_address")
  private String postalAddress;
  @Column(name = "phone_number")
  private String phoneNumber;
}
