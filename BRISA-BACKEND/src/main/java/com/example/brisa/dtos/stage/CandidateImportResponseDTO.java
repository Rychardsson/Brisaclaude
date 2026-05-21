package com.example.brisa.dtos.stage;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CandidateImportResponseDTO {
    private int totalProcessed;
    private int successfullyInserted;
    private int alreadyInStage;
    private int newPeopleCreated;
    private List<String> duplicateCandidates; // Candidatos que já estavam na etapa
    private List<String> createdPeople; // Pessoas que foram criadas durante a importação
    private List<CandidateRowErrorDTO> rowErrors; // Erros por linha (ex.: conflito de nivelamento)
}
