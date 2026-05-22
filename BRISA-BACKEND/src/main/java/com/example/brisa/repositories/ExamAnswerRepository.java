package com.example.brisa.repositories;

import com.example.brisa.models.exam.ExamAnswerModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ExamAnswerRepository extends JpaRepository<ExamAnswerModel, Long> {
    @Query("""
        SELECT ea
        FROM ExamAnswerModel ea
        JOIN FETCH ea.people
        JOIN FETCH ea.examQuestion eq
        JOIN FETCH eq.exam
        LEFT JOIN FETCH eq.subject
        WHERE eq.exam.id = :examId
    """)
    List<ExamAnswerModel> findAllByExamIdWithRelations(@Param("examId") Long examId);

    @Modifying
    @Query("""
        DELETE FROM ExamAnswerModel ea
        WHERE ea.people.id = :peopleId
          AND ea.examQuestion.exam.id = :examId
    """)
    void deleteByPeopleIdAndExamId(@Param("peopleId") Long peopleId, @Param("examId") Long examId);
}

