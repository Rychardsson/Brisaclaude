package com.example.brisa.controllers;

import com.example.brisa.dtos.course.CourseEvaluationRequestDTO;
import com.example.brisa.dtos.course.CourseEvaluationResponseDTO;
import com.example.brisa.services.CourseEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/courses/{courseId}/evaluations")
@RequiredArgsConstructor
public class CourseEvaluationController {

    private final CourseEvaluationService courseEvaluationService;

    @GetMapping
    public ResponseEntity<List<CourseEvaluationResponseDTO>> list(@PathVariable Long courseId) {
        return ResponseEntity.ok(courseEvaluationService.listByCourse(courseId));
    }

    @PostMapping
    public ResponseEntity<CourseEvaluationResponseDTO> create(
            @PathVariable Long courseId,
            @RequestBody CourseEvaluationRequestDTO request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(courseEvaluationService.create(courseId, request));
    }

    @PutMapping("/{evaluationId}")
    public ResponseEntity<CourseEvaluationResponseDTO> update(
            @PathVariable Long courseId,
            @PathVariable Long evaluationId,
            @RequestBody CourseEvaluationRequestDTO request
    ) {
        return ResponseEntity.ok(courseEvaluationService.update(courseId, evaluationId, request));
    }

    @DeleteMapping("/{evaluationId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long courseId,
            @PathVariable Long evaluationId
    ) {
        courseEvaluationService.delete(courseId, evaluationId);
        return ResponseEntity.noContent().build();
    }
}
