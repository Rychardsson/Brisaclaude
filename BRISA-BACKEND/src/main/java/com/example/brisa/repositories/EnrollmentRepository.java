package com.example.brisa.repositories;

import com.example.brisa.models.EnrollmentModel;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<EnrollmentModel, Long> {
    List<EnrollmentModel> findByClassModelId(Long classId);
    List<EnrollmentModel> findByPeopleId(Long peopleId);

    Optional<EnrollmentModel> findByPeopleIdAndClassModelId(Long peopleId, Long classId);
    Optional<EnrollmentModel> findByPeopleIdAndClassModelIdAndAcademicRoleId(Long peopleId, Long classId, Long academicRoleId);

    @Query("""
        SELECT DISTINCT e
        FROM EnrollmentModel e
        JOIN FETCH e.people
        JOIN FETCH e.classModel c
        JOIN FETCH c.program
        LEFT JOIN FETCH c.location
        LEFT JOIN FETCH e.academicRole
    """)
    List<EnrollmentModel> findAllWithRelations();

    @Query("""
        SELECT DISTINCT e
        FROM EnrollmentModel e
        JOIN FETCH e.people
        JOIN FETCH e.classModel c
        JOIN FETCH c.program
        LEFT JOIN FETCH c.location
        LEFT JOIN FETCH e.academicRole
        WHERE c.id = :classId
    """)
    List<EnrollmentModel> findByClassIdWithRelations(@Param("classId") Long classId);
}
