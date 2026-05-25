package com.example.brisa.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
@Table(name = "peoples")
public class PeopleModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String cpf;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "education_level")
    private String educationLevel;

    private String address;

    @Column(name = "address_extra")
    private String addressExtra;

    private String city;

    @Column(name = "state")
    private String state;

    private String gender;

    @Column(name = "quota_category")
    private String quotaCategory;

    private String phone;

    private String linkedin;

    @Column(name = "zip_code")
    private String zipCode;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "institution_name")
    private String institutionName;

    @Column(name = "course_name")
    private String courseName;

    @Column(name = "education_status")
    private String educationStatus;

    @Column(name = "education_completion_date")
    private LocalDate educationCompletionDate;

    @Column(name = "soft_deleted")
    private Boolean softDeleted = false;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
