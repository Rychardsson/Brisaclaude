package com.example.brisa.models.course;

import java.time.LocalDate;

import com.example.brisa.models.ClassModel;
import com.example.brisa.models.PeopleModel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;


@Data
@Entity
@Table(name = "course_progressions")
public class CourseProgressionModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private CourseModel course;

    @ManyToOne
    @JoinColumn(name = "people_id")
    private PeopleModel people;

    @ManyToOne
    @JoinColumn(name = "class_id")
    private ClassModel classModel;

    @Column(name = "date")
    private LocalDate date;

    @Column(name = "grade")
    private Double grade;

    @Column(name = "frequency")
    private Double frequency;

    @Column(name = "completion_percentage")
    private double completionPercentage;

    private String status; // concluído, em andamento, não iniciado

    // ✅ Novo campo: último acesso do aluno ao curso (Opção B — campo novo no banco)
    @Column(name = "last_access")
    private LocalDate lastAccess;
}
