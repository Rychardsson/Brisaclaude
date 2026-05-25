package com.example.brisa.repositories;

import com.example.brisa.models.exam.ExamQuestionModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExamQuestionRepository extends JpaRepository<ExamQuestionModel, Long> {
    List<ExamQuestionModel> findByExamIdOrderByQuestionNumberAsc(Long examId);
    Optional<ExamQuestionModel> findByExamIdAndQuestionNumber(Long examId, Integer questionNumber);
}

