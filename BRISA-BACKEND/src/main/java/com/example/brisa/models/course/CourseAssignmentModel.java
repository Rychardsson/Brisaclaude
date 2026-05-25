package com.example.brisa.models.course;

import com.example.brisa.models.ClassModel;
import com.example.brisa.models.AdvisorModel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "course_assignments", indexes = {
    @Index(name = "idx_course_assignment_class", columnList = "class_id"),
    @Index(name = "idx_course_assignment_course", columnList = "course_id")
})
public class CourseAssignmentModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private CourseModel course;

    @ManyToOne
    @JoinColumn(name = "class_id", nullable = false)
    private ClassModel classModel;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "required", nullable = false)
    private boolean required = true;

    @ManyToOne
    @JoinColumn(name = "advisor_id")
    private AdvisorModel advisor;
}
