package com.example.brisa.repositories;

import com.example.brisa.models.ProgramInstitutionModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProgramInstitutionRepository extends JpaRepository<ProgramInstitutionModel, Long> {
    boolean existsByProgramIdAndInstitutionId(Long programId, Long institutionId);
}
