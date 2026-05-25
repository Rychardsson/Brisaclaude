package com.example.brisa.dtos.stage;

import com.example.brisa.enums.StageStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CandidateImportDTO {
    private String name;
    private String email;
    private String cpf;
    private StageStatus status;
    private String notes;
    private String phone;
    private String gender;
    private String quotaCategory;
    private LocalDate birthDate;
    private String state;
    private String city;
    private String educationLevel;
    private String institutionName;
    private String courseName;
    private String educationStatus;
    private LocalDate educationCompletionDate;
    private int row; // número da linha no arquivo Excel (1-based)
}
