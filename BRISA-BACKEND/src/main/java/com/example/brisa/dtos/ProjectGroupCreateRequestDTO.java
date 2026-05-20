package com.example.brisa.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectGroupCreateRequestDTO {
    private String projectTheme; // nome do projeto
    private String description; // resumo do projeto
    private Long projectCompanyId; // empresa/instituição (local onde o programa ocorre)
    private String sponsorCompany; // empresa do grupo (patrocinadora, texto livre)
    private Long leaderId; // orientador (people_id)
    private List<Long> memberIds; // alunos do grupo
    private DayOfWeek weeklyMeetingDay; // dia da semana para reuniões
    private LocalDate firstMeetingDate; // primeira data de reunião
    private String repositoryLink; // opcional
}
