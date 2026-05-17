package com.example.brisa.repositories;

import com.example.brisa.models.InstitutionModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InstitutionRepository extends JpaRepository<InstitutionModel, Long> {
    Optional<InstitutionModel> findByCode(String code);
    Optional<InstitutionModel> findByCodeIgnoreCase(String code);
    Optional<InstitutionModel> findByName(String name);
    Optional<InstitutionModel> findByNameIgnoreCase(String name);
}
