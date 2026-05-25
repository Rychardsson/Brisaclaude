package com.example.brisa.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.util.HashSet;
import java.util.Set;

// Model de instituições
@Data
@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"programMemberships"})
@Table(name = "institutions")
public class InstitutionModel{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;
    private String code; // não sei bem que código é esse, precisa confirmar se é o id ou outro código
    private String name; // nome da instituição
    private String acronym; // Sigla

    @Column(name = "state")
    private String state;
    
    @Column(name = "local_coordinator")
    private String localCoordinatorName; // Apenas texto, ou link para Person se necessário

    @JsonIgnore
    @OneToMany(mappedBy = "institution", orphanRemoval = true)
    private Set<ProgramInstitutionModel> programMemberships = new HashSet<>();
}
