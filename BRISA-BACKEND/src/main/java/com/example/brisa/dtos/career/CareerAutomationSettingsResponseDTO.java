package com.example.brisa.dtos.career;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record CareerAutomationSettingsResponseDTO(
        Long id,
        boolean enabled,
        Long programId,
        String programName,
        Long classId,
        String classCode,
        String subject,
        String message,
        List<Integer> checkpoints,
        LocalDateTime lastTestAt,
        LocalDateTime updatedAt
) {
}
