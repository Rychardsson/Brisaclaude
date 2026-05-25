package com.example.brisa.models.course;

import com.example.brisa.models.KnowledgeAreaModel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "courses")
public class CourseModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // nome do curso

    @Column(unique = true)
    private String code; // codigo_curso

    private String description; // descrição do curso
    
    private double workload; // carga horária em horas

    @Column(name = "inclusion_date")
    private LocalDate inclusionDate; // data_inclusao
    
    @ManyToOne
    @JoinColumn(name = "knowledge_area_id")
    private KnowledgeAreaModel knowledgeArea; 

    @ManyToOne
    @JoinColumn(name = "sub_area_id")
    private KnowledgeAreaModel subArea;

    @PrePersist
    private void prePersist() {
        if (inclusionDate == null) {
            inclusionDate = LocalDate.now();
        }
    }
    
}
