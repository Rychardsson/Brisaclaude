package com.example.brisa.dtos.program;

import com.example.brisa.models.ProgramAddendumModel;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProgramAddendumResponseDTO(
        Long id,
        Long programId,
        Integer addendumNumber,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal value
) {
    public ProgramAddendumResponseDTO(ProgramAddendumModel model) {
        this(
                model.getId(),
                model.getProgram() != null ? model.getProgram().getId() : null,
                model.getAddendumNumber(),
                model.getStartDate(),
                model.getEndDate(),
                model.getValue()
        );
    }
}

