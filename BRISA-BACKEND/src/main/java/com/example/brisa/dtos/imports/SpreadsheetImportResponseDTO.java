package com.example.brisa.dtos.imports;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpreadsheetImportResponseDTO {
    private int totalProcessed;
    private int successfullyInserted;
    private int updated;
    private int alreadyExists;
    private int relatedRecordsCreated;
    private List<String> duplicateItems = new ArrayList<>();
    private List<String> errors = new ArrayList<>();
}
