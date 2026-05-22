package com.example.brisa.dtos.exam;

import java.util.List;

public record ExamRespondentProfileDTO(
        List<MetricDTO> courseDistribution,
        List<MetricDTO> topCourseAccuracy,
        List<MetricDTO> quotaAccuracy,
        List<MetricDTO> topCityAccuracy
) {
    public record MetricDTO(String label, double value, long count) {}
}

