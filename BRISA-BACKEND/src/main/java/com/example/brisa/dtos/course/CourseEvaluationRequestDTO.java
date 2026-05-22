package com.example.brisa.dtos.course;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CourseEvaluationRequestDTO {
    private String code;
    private String type;
    private String name;
    private Double minimumScore;
    private Double maximumScore;
    private LocalDate eventDate;
}

