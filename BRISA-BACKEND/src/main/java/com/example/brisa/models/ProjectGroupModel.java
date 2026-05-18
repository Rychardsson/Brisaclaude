package com.example.brisa.models;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "project_groups")
public class ProjectGroupModel {
    //  (cod_programa, cod_turma, cod_grupo, , tema_projeto, empresa_projeto, link_repositprio_projeto
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_theme")
    private String projectTheme; // nome projeto
    
    @ManyToOne
    @JoinColumn(name = "class_id", nullable = false)
    private ClassModel classModel; // turma do projeto

    @Column(name = "description")
    private String description; // descrição do grupo de projeto
    
    @ManyToOne
    @JoinColumn(name = "project_company_id")
    private InstitutionModel projectCompany; // empresa do projeto
    
    @ManyToOne
    @JoinColumn(name = "leader_id")
    private PeopleModel leader; // orientador do grupo

    @Column(name = "repository_link")
    private String repositoryLink; // link do repositório do projeto

    @OneToMany(mappedBy = "projectGroup")
    private Set<PeopleProjectGroupModel> members = new HashSet<>(); // membros do grupo de projeto

    @Column(name = "weekly_meeting_day")
    @Enumerated(EnumType.ORDINAL)
    private DayOfWeek weeklyMeetingDay; // dia da semana para reuniões (MONDAY, TUESDAY, etc)

    @Column(name = "first_meeting_date")
    private LocalDate firstMeetingDate; // data da primeira reunião

    @OneToMany(mappedBy = "projectGroup")
    private Set<ProjectGroupMeetingModel> meetings = new HashSet<>(); // reuniões semanais do grupo

}
