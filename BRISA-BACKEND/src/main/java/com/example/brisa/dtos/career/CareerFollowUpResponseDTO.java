package com.example.brisa.dtos.career;

import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
public record CareerFollowUpResponseDTO(
        Long id,
        String key,
        Long peopleId,
        Long enrollmentId,
        Long classId,
        Long programId,
        String personName,
        String classCode,
        String programName,
        LocalDate surveyDate,
        String status,
        String company,
        String position,
        String notes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
