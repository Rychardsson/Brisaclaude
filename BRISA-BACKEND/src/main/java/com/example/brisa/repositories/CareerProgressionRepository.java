package com.example.brisa.repositories;

import com.example.brisa.models.CareerProgressionModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CareerProgressionRepository extends JpaRepository<CareerProgressionModel, Long> {

    @Query("""
        SELECT DISTINCT cp
        FROM CareerProgressionModel cp
        JOIN FETCH cp.people p
        LEFT JOIN FETCH cp.enrollment e
        LEFT JOIN FETCH cp.classModel c
        LEFT JOIN FETCH cp.program pr
        WHERE (:peopleId IS NULL OR p.id = :peopleId)
          AND (:classId IS NULL OR c.id = :classId)
          AND (:programId IS NULL OR pr.id = :programId)
    """)
    List<CareerProgressionModel> findAllWithRelations(
            @Param("peopleId") Long peopleId,
            @Param("classId") Long classId,
            @Param("programId") Long programId
    );
}
