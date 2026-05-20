package com.example.brisa.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "career_automation_dispatches",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"enrollment_id", "checkpoint_months"})
        },
        indexes = {
                @Index(name = "idx_career_dispatch_status", columnList = "status"),
                @Index(name = "idx_career_dispatch_due_date", columnList = "due_date")
        }
)
public class CareerAutomationDispatchModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "people_id", nullable = false)
    private PeopleModel people;

    @ManyToOne
    @JoinColumn(name = "enrollment_id", nullable = false)
    private EnrollmentModel enrollment;

    @ManyToOne
    @JoinColumn(name = "class_id")
    private ClassModel classModel;

    @ManyToOne
    @JoinColumn(name = "program_id")
    private ProgramModel program;

    @Column(name = "checkpoint_months", nullable = false)
    private Integer checkpointMonths;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "recipient_email", nullable = false)
    private String recipientEmail;

    @Column(name = "subject_snapshot", length = 255)
    private String subjectSnapshot;

    @Column(name = "status", nullable = false, length = 32)
    private String status;

    @Column(name = "attempt_count", nullable = false)
    private Integer attemptCount = 0;

    @Column(name = "last_attempt_at")
    private LocalDateTime lastAttemptAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "last_error", length = 1000)
    private String lastError;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
