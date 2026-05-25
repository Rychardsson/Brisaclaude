package com.example.brisa.models;

import java.time.LocalDate;

import com.example.brisa.models.roles.AcademicRoleModel;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "enrollments", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"people_id", "class_id", "academic_role_id"})
})
public class EnrollmentModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "people_id", nullable = false)
    private PeopleModel people; // A pessoa (aluno, orientador, etc.)

    @ManyToOne
    @JoinColumn(name = "class_id", nullable = false)
    private ClassModel classModel; // A turma

    @ManyToOne
    @JoinColumn(name = "academic_role_id", nullable = false)
    private AcademicRoleModel academicRole; // Papel na turma: Aluno, Orientador, Coordenador, etc.

    @Column(name = "enrollment_date")
    private LocalDate enrollmentDate; // Data de matrícula

    @Column(name = "status")
    private String status; // Realizado, Não-realizado, Em andamento

    @Column(name = "completion_date")
    private LocalDate completionDate; // Data de conclusão se concluir

    @Column(name = "grade")
    private Double grade; // Nota final 

    @Column(name = "frequency")
    private Double frequency; // Frequência percentual do aluno
}
