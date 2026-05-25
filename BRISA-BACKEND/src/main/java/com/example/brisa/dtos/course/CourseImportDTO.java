package com.example.brisa.dtos.course;

import java.time.LocalDate;

public record CourseImportDTO(
    String code,
    String name,
    String description,
    String competencia,
    String knowledgeArea,
    String subArea,
    Double workload,
    LocalDate inclusionDate
) {}
