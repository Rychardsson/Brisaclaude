package com.example.brisa.models;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "programs")
public class ProgramModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String code;

    private String name;

    private String contractNumber;

    @Column(name = "executor_name")
    private String executorName;

    @Column(name = "funding_entity")
    private String fundingEntity;

    @Column(name = "general_coordinator")
    private String generalCoordinator;

    @Column(name = "program_value", precision = 14, scale = 2)
    private BigDecimal programValue;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "target_audience", length = 2000)
    private String targetAudience;

    @Column(name = "leveling_modality")
    private String levelingModality;

    @Column(name = "leveling_duration")
    private String levelingDuration;

    @Column(name = "immersion_duration")
    private String immersionDuration;

    @Column(name = "immersion_workload_hours")
    private Integer immersionWorkloadHours;

    @Column(name = "quota_criteria", length = 2000)
    private String quotaCriteria;

    @Column(name = "evaluation_criteria", length = 4000)
    private String evaluationCriteria;
 
    @Column(name = "executor", length = 500)
    private String executor;
 
    @Column(name = "location", length = 500)
    private String location;
 
    @Column(name = "support_email")
    private String supportEmail;
 
    @Column(name = "official_website")
    private String officialWebsite;
 
    @Column(name = "main_locality")
    private String mainLocality;
 
    @Column(name = "observations", length = 4000)
    private String observations;
 
    @Column(name = "partner_names", length = 4000)
    private String partnerNames;

    @OneToMany(mappedBy = "program", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<ProgramInstitutionModel> programInstitutions = new HashSet<>();

    @OneToMany(mappedBy = "program", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ClassModel> classes = new HashSet<>();
}
