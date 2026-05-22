package com.example.brisa.dtos.exam;

import java.util.List;

public record ApprovedRankingResponseDTO(
        int totalVacancies,
        long totalCandidates,
        List<ApprovedCandidateDTO> approved,
        List<ApprovedCandidateDTO> waitlist,
        List<ApprovedCandidateDTO> rejected
) {
    public record ApprovedCandidateDTO(
            Long peopleId,
            String name,
            String cpf,
            String quota,
            String city,
            Integer age,
            double examScore,
            long completedCourses,
            int cityBonus,
            double totalPoints,
            int rankingPosition,
            String decision
    ) {}
}
