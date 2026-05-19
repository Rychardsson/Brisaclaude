package com.example.brisa.repositories;

import com.example.brisa.models.ClassModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface ClassRepository extends JpaRepository<ClassModel, Long> {
    Optional<ClassModel> findByCode(String code);
    Optional<ClassModel> findByCodeIgnoreCase(String code);
    boolean existsByCode(String code);
    List<ClassModel> findByProgramId(Long programId);
    long countByProgramId(Long programId);

    // ✅ Retorna a contagem de turmas agrupada por programa em uma única query
    @Query("SELECT c.program.id, COUNT(c) FROM ClassModel c GROUP BY c.program.id")
    List<Object[]> countClassesGroupedByProgram();
}
