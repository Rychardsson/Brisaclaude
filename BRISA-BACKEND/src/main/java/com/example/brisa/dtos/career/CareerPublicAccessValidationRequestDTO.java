package com.example.brisa.dtos.career;

import lombok.Data;

@Data
public class CareerPublicAccessValidationRequestDTO {
    private String email;
    private String token;
}
