package com.example.brisa.repositories;

import com.example.brisa.models.course.CourseEvaluationModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CourseEvaluationRepository extends JpaRepository<CourseEvaluationModel, Long> {
    List<CourseEvaluationModel> findByCourseIdOrderByEventDateAscIdAsc(Long courseId);
    Optional<CourseEvaluationModel> findByCourseIdAndCodeIgnoreCase(Long courseId, String code);
    long countByCourseId(Long courseId);
}

