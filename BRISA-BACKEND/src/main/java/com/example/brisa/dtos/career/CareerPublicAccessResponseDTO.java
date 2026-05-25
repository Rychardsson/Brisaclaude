package com.example.brisa.dtos.career;

import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
public record CareerPublicAccessResponseDTO(
        String email,
        String token,
        String personName,
        String programName,
        String classCode,
        Integer checkpointMonths,
        LocalDate completionDate,
        LocalDate dueDate,
        boolean alreadySubmitted,
        LocalDateTime respondedAt,
        CareerFollowUpResponseDTO latestFollowUp
) {
}
