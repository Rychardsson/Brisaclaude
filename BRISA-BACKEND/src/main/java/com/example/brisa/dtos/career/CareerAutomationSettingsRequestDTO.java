package com.example.brisa.dtos.career;

import lombok.Data;

import java.util.List;

@Data
public class CareerAutomationSettingsRequestDTO {
    private boolean enabled;
    private Long programId;
    private Long classId;
    private String subject;
    private String message;
    private List<Integer> checkpoints;
}
