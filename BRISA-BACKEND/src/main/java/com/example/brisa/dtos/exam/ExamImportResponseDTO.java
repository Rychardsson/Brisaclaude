package com.example.brisa.dtos.exam;

import java.util.List;

public record ExamImportResponseDTO(
        Long examId,
        String examCode,
        int participantsProcessed,
        int questionsDetected,
        List<String> warnings
) {
}

