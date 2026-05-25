package com.example.brisa.repositories;

import com.example.brisa.models.course.CourseModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<CourseModel, Long> {
	Optional<CourseModel> findByNameIgnoreCase(String name);
	Optional<CourseModel> findByCodeIgnoreCase(String code);
}
