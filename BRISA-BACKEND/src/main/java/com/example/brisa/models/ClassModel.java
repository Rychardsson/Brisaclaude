package com.example.brisa.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDate;

@Data
@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"program", "location"})
@Table(name = "classes")
public class ClassModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(unique = true)
    private String code;

    @JsonIgnoreProperties({"classes"})
    @ManyToOne
    @JoinColumn(name = "program_id", nullable = false)
    private ProgramModel program;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @JsonProperty("localidade")
    @Column(name = "locality")
    private String locality;

    @JsonIgnoreProperties({"programMemberships"})
    @ManyToOne
    @JoinColumn(name = "location_id")
    private InstitutionModel location;

    @Column(name = "default_selection_capacity")
    private Integer defaultSelectionCapacity = 350;

    @Column(name = "default_leveling_capacity")
    private Integer defaultLevelingCapacity = 250;

    @Column(name = "default_immersion_capacity")
    private Integer defaultImmersionCapacity = 50;

    @Column(name = "immersion_team_size")
    private Integer immersionTeamSize = 5;

    @Column(name = "qtd_vagas")
    private Integer qtdVagas;

    @Column(name = "publication_date")
    private LocalDate publicationDate;

    @Column(name = "application_start_date")
    private LocalDate applicationStartDate;

    @Column(name = "application_end_date")
    private LocalDate applicationEndDate;

    @Column(name = "leveling_selection_announcement_date")
    private LocalDate levelingSelectionAnnouncementDate;

    @Column(name = "leveling_confirmation_start_date")
    private LocalDate levelingConfirmationStartDate;

    @Column(name = "leveling_confirmation_end_date")
    private LocalDate levelingConfirmationEndDate;

    @Column(name = "leveling_start_date")
    private LocalDate levelingStartDate;

    @Column(name = "leveling_end_date")
    private LocalDate levelingEndDate;

    @Column(name = "leveling_final_exam_date")
    private LocalDate levelingFinalExamDate;

    @Column(name = "immersion_selection_announcement_date")
    private LocalDate immersionSelectionAnnouncementDate;

    @Column(name = "immersion_confirmation_start_date")
    private LocalDate immersionConfirmationStartDate;

    @Column(name = "immersion_confirmation_end_date")
    private LocalDate immersionConfirmationEndDate;

    @Column(name = "immersion_start_date")
    private LocalDate immersionStartDate;

    @Column(name = "immersion_end_date")
    private LocalDate immersionEndDate;

    @Column(name = "partial_evaluation_date")
    private LocalDate partialEvaluationDate;

    @Column(name = "final_evaluation_date")
    private LocalDate finalEvaluationDate;

    @Column(name = "certificate_issue_date")
    private LocalDate certificateIssueDate;
}
