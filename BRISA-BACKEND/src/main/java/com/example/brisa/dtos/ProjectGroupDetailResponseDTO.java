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
public class ProjectGroupDetailResponseDTO {
    private Long id;
    private String projectTheme;
    private String description;
    private String projectCompanyName;
    private Long projectCompanyId;
    private String leaderName;
    private Long leaderId;
    private List<GroupMemberDTO> members;
    private DayOfWeek weeklyMeetingDay;
    private LocalDate firstMeetingDate;
    private String repositoryLink;
    private List<GroupMeetingDTO> meetings;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GroupMemberDTO {
        private Long id;
        private String name;
        private String email;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GroupMeetingDTO {
        private Long id;
        private LocalDate meetingDate;
        private String status;
    }
}
