package com.example.brisa.dtos.program;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ProgramAddendumRequestDTO {
    private Integer addendumNumber;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal value;
}

