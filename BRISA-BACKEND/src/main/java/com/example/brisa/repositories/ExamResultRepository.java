package com.example.brisa.repositories;

import com.example.brisa.models.exam.ExamResultModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ExamResultRepository extends JpaRepository<ExamResultModel, Long> {
    Optional<ExamResultModel> findByExamIdAndPeopleId(Long examId, Long peopleId);

    @Query("""
        SELECT er
        FROM ExamResultModel er
        JOIN FETCH er.people
        JOIN FETCH er.exam exam
        JOIN FETCH exam.classModel
        WHERE exam.classModel.id = :classId
    """)
    List<ExamResultModel> findByClassIdWithRelations(@Param("classId") Long classId);

    @Query("""
        SELECT er
        FROM ExamResultModel er
        JOIN FETCH er.people
        JOIN FETCH er.exam exam
        WHERE exam.id = :examId
    """)
    List<ExamResultModel> findByExamIdWithRelations(@Param("examId") Long examId);
}
