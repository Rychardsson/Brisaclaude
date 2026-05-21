package com.example.brisa.dtos.stage;

import com.example.brisa.enums.StageStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CandidateImportDTO {
    private String name;
    private String email;
    private String cpf;
    private StageStatus status;
    private String notes;
    private int row; // número da linha no arquivo Excel (1-based)
}
