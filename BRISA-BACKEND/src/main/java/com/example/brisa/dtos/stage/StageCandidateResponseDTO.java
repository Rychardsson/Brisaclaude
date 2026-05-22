package com.example.brisa.dtos.stage;

import com.example.brisa.enums.StageStatus;
import com.example.brisa.models.StageCandidateModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StageCandidateResponseDTO {
    private Long id;
    private Long stageId;
    private String stageName;
    private Long peopleId;
    private String peopleName;
    private String peopleCpf;
    private String peopleEmail;
    private String peopleGender;
    private LocalDate peopleBirthDate;
    private String peopleCity;
    private String peopleState;
    private String peopleQuotaCategory;
    private String peopleEducationLevel;
    private String peopleInstitutionName;
    private StageStatus status;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public StageCandidateResponseDTO(StageCandidateModel stageCandidate) {
        this.id = stageCandidate.getId();
        this.stageId = stageCandidate.getStage().getId();
        this.stageName = stageCandidate.getStage().getName();
        this.peopleId = stageCandidate.getPeople().getId();
        this.peopleName = stageCandidate.getPeople().getName();
        this.peopleCpf = stageCandidate.getPeople().getCpf();
        this.peopleEmail = stageCandidate.getPeople().getEmail();
        this.peopleGender = stageCandidate.getPeople().getGender();
        this.peopleBirthDate = stageCandidate.getPeople().getBirthDate();
        this.peopleCity = stageCandidate.getPeople().getCity();
        this.peopleState = stageCandidate.getPeople().getState();
        this.peopleQuotaCategory = stageCandidate.getPeople().getQuotaCategory();
        this.peopleEducationLevel = stageCandidate.getPeople().getEducationLevel();
        this.peopleInstitutionName = stageCandidate.getPeople().getInstitutionName();
        this.status = stageCandidate.getStatus();
        this.notes = stageCandidate.getNotes();
        this.createdAt = stageCandidate.getCreatedAt();
        this.updatedAt = stageCandidate.getUpdatedAt();
    }
}
