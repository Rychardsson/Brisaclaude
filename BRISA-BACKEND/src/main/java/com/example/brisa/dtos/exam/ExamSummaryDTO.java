package com.example.brisa.dtos.exam;

import java.util.List;

public record ExamSummaryDTO(
        String examCode,
        String examDate,
        int totalQuestions,
        int totalParticipants,
        long femaleParticipants,
        long maleParticipants,
        long zeroScoreCount,
        long highestScoreCount,
        double highestScore,
        double averageScore,
        List<ScoreBucketDTO> scoreDistribution,
        List<QuestionPerformanceDTO> bestQuestions,
        List<QuestionPerformanceDTO> worstQuestions
) {
    public record ScoreBucketDTO(String label, long count) {}
    public record QuestionPerformanceDTO(Integer questionNumber, String subject, long correctAnswers, long totalAnswers, double successRate) {}
}
