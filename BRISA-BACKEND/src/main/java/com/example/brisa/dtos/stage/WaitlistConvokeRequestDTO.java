package com.example.brisa.dtos.stage;

import lombok.Data;

@Data
public class WaitlistConvokeRequestDTO {
    private Integer convokeCount;
    private String deadline;
    private String notes;
}

