package com.example.brisa.dtos.exam;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ApprovedRankingRequestDTO {
    private Integer totalVacancies;
    private Double pointsPerCompletedCourse = 2.5;
    private Integer bonusPerPriorityCity = 2;
    private List<String> priorityCities = new ArrayList<>();
    private Integer amplaConcorrenciaSeats = 0;
    private Integer pcdSeats = 0;
    private Integer negroPardoSeats = 0;
    private Integer mulheresSeats = 0;
    private Integer age45Seats = 0;
}
