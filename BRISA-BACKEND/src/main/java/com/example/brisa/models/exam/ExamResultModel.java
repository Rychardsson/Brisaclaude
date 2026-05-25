package com.example.brisa.models.exam;

import com.example.brisa.models.PeopleModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;

@Data
@Entity
@Table(name = "exam_results", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"exam_id", "people_id"})
})
public class ExamResultModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "exam_id", nullable = false)
    private ExamModel exam;

    @ManyToOne
    @JoinColumn(name = "people_id", nullable = false)
    private PeopleModel people;

    @Column(name = "total_score")
    private Double totalScore;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;
}

