package com.example.brisa.repositories;

import com.example.brisa.models.ProgramModel;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface ProgramRepository extends JpaRepository<ProgramModel, Long> {
    Optional<ProgramModel> findByCode(String code);
    Optional<ProgramModel> findByCodeIgnoreCase(String code);
    Optional<ProgramModel> findByNameIgnoreCase(String name);
    boolean existsByCode(String code);

    @Query("""
        SELECT DISTINCT p
        FROM ProgramModel p
        LEFT JOIN FETCH p.programInstitutions pi
        LEFT JOIN FETCH pi.institution
        LEFT JOIN FETCH p.classes c
        LEFT JOIN FETCH c.location
    """)
    List<ProgramModel> findAllWithRelations();
}
