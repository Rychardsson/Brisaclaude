package com.example.brisa.dtos.course;

import com.example.brisa.models.course.CourseEvaluationModel;

import java.time.LocalDate;

public record CourseEvaluationResponseDTO(
        Long id,
        Long courseId,
        String code,
        String type,
        String name,
        Double minimumScore,
        Double maximumScore,
        LocalDate eventDate
) {
    public CourseEvaluationResponseDTO(CourseEvaluationModel model) {
        this(
                model.getId(),
                model.getCourse() != null ? model.getCourse().getId() : null,
                model.getCode(),
                model.getType(),
                model.getName(),
                model.getMinimumScore(),
                model.getMaximumScore(),
                model.getEventDate()
        );
    }
}

