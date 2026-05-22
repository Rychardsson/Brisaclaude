package com.example.brisa.models.course;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "course_evaluations")
public class CourseEvaluationModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private CourseModel course;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String name;

    @Column(name = "minimum_score")
    private Double minimumScore;

    @Column(name = "maximum_score")
    private Double maximumScore;

    @Column(name = "event_date")
    private LocalDate eventDate;
}

