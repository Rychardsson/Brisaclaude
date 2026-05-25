package com.example.brisa.dtos.people;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PeopleImportDTO {
    private String name;
    private String email;
    private String cpf;
    private String phone;
    private String linkedin;
    private String educationLevel;
    private String address;
    private String addressExtra;
    private String state;
    private String zipCode;
    private String city;
    private String gender;
    private String quotaCategory;
    private LocalDate birthDate;
    private String institutionName;
    private String courseName;
    private String educationStatus;
    private LocalDate educationCompletionDate;
    private Long programaId;
    private Long turmaId;
    private Long etapaId;
    private String statusInicial;
}
