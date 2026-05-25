package com.example.brisa.repositories;

import com.example.brisa.models.exam.ExamModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExamRepository extends JpaRepository<ExamModel, Long> {
    List<ExamModel> findByClassModelIdOrderByExamDateDescIdDesc(Long classId);
    Optional<ExamModel> findFirstByClassModelIdOrderByExamDateDescIdDesc(Long classId);
    Optional<ExamModel> findByClassModelIdAndNameIgnoreCase(Long classId, String name);
    Optional<ExamModel> findByClassModelIdAndCodeIgnoreCase(Long classId, String code);
}
