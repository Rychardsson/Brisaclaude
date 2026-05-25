package com.example.brisa.dtos.stage;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CandidateRowErrorDTO {
    private int row; // número da linha (1-based)
    private List<String> messages; // mensagens de erro para a linha
}
