package com.example.brisa.dtos.career;

import lombok.Data;

@Data
public class CareerPublicFollowUpSubmissionRequestDTO {
    private String email;
    private String token;
    private String surveyDate;
    private String status;
    private String company;
    private String position;
    private String notes;
}
