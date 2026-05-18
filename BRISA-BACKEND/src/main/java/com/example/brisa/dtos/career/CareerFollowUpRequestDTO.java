package com.example.brisa.dtos.career;

import lombok.Data;

@Data
public class CareerFollowUpRequestDTO {
    private Long peopleId;
    private Long enrollmentId;
    private Long classId;
    private Long programId;
    private String surveyDate;
    private String status;
    private String company;
    private String position;
    private String notes;
}
