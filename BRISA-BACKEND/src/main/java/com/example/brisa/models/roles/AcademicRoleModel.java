package com.example.brisa.models.roles;

import jakarta.persistence.*;
import lombok.*;
// Essa tabela serve para definir os diferentes papéis acadêmicos.
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "academic_roles")
public class AcademicRoleModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; // Ex: "ALUNO", "ORIENTADOR"

    private String description; 

    
    public AcademicRoleModel(String name, String description) {
        this.name = name;
        this.description = description;
    }
}