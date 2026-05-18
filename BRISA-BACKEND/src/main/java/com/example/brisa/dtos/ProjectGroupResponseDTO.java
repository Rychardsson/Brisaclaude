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
public class ProjectGroupResponseDTO {
    private Long id;
    private String projectTheme;
    private String description;
    private String projectCompanyName;
    private Long projectCompanyId;
    private String leaderName;
    private Long leaderId;
    private Integer memberCount;
    private DayOfWeek weeklyMeetingDay;
    private LocalDate firstMeetingDate;
    private String repositoryLink;
    private Integer upcomingMeetingsCount;
}
