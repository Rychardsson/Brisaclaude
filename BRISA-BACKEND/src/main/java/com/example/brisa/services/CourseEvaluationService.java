package com.example.brisa.services;

import com.example.brisa.dtos.course.CourseEvaluationRequestDTO;
import com.example.brisa.dtos.course.CourseEvaluationResponseDTO;
import com.example.brisa.exceptions.ResourceNotFoundException;
import com.example.brisa.exceptions.ValidationException;
import com.example.brisa.models.course.CourseEvaluationModel;
import com.example.brisa.models.course.CourseModel;
import com.example.brisa.repositories.CourseEvaluationRepository;
import com.example.brisa.repositories.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseEvaluationService {

    private final CourseRepository courseRepository;
    private final CourseEvaluationRepository courseEvaluationRepository;

    @Transactional(readOnly = true)
    public List<CourseEvaluationResponseDTO> listByCourse(Long courseId) {
        ensureCourse(courseId);
        return courseEvaluationRepository.findByCourseIdOrderByEventDateAscIdAsc(courseId).stream()
                .map(CourseEvaluationResponseDTO::new)
                .toList();
    }

    @Transactional
    public CourseEvaluationResponseDTO create(Long courseId, CourseEvaluationRequestDTO request) {
        CourseModel course = ensureCourse(courseId);
        validate(courseId, null, request);
        if (courseEvaluationRepository.countByCourseId(courseId) >= 4) {
            throw new ValidationException(List.of("Cada curso pode ter no máximo 4 avaliações."));
        }

        CourseEvaluationModel model = new CourseEvaluationModel();
        model.setCourse(course);
        apply(model, request);
        return new CourseEvaluationResponseDTO(courseEvaluationRepository.save(model));
    }

    @Transactional
    public CourseEvaluationResponseDTO update(Long courseId, Long evaluationId, CourseEvaluationRequestDTO request) {
        ensureCourse(courseId);
        validate(courseId, evaluationId, request);

        CourseEvaluationModel model = courseEvaluationRepository.findById(evaluationId)
                .filter(item -> item.getCourse() != null && item.getCourse().getId().equals(courseId))
                .orElseThrow(() -> new ResourceNotFoundException("Avaliação não encontrada."));
        apply(model, request);
        return new CourseEvaluationResponseDTO(courseEvaluationRepository.save(model));
    }

    @Transactional
    public void delete(Long courseId, Long evaluationId) {
        ensureCourse(courseId);
        CourseEvaluationModel model = courseEvaluationRepository.findById(evaluationId)
                .filter(item -> item.getCourse() != null && item.getCourse().getId().equals(courseId))
                .orElseThrow(() -> new ResourceNotFoundException("Avaliação não encontrada."));
        courseEvaluationRepository.delete(model);
    }

    private CourseModel ensureCourse(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Curso não encontrado."));
    }

    private void validate(Long courseId, Long evaluationId, CourseEvaluationRequestDTO request) {
        if (request.getCode() == null || request.getCode().trim().isEmpty()) {
            throw new ValidationException(List.of("Código da avaliação é obrigatório."));
        }
        if (request.getType() == null || request.getType().trim().isEmpty()) {
            throw new ValidationException(List.of("Tipo da avaliação é obrigatório."));
        }
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new ValidationException(List.of("Nome da avaliação é obrigatório."));
        }
        if (request.getMaximumScore() != null && request.getMinimumScore() != null && request.getMaximumScore() < request.getMinimumScore()) {
            throw new ValidationException(List.of("A pontuação máxima não pode ser menor que a mínima."));
        }

        boolean duplicate = courseEvaluationRepository.findByCourseIdAndCodeIgnoreCase(courseId, request.getCode().trim())
                .filter(item -> evaluationId == null || !item.getId().equals(evaluationId))
                .isPresent();
        if (duplicate) {
            throw new ValidationException(List.of("Já existe uma avaliação com esse código para o curso."));
        }
    }

    private void apply(CourseEvaluationModel model, CourseEvaluationRequestDTO request) {
        model.setCode(request.getCode().trim());
        model.setType(request.getType().trim());
        model.setName(request.getName().trim());
        model.setMinimumScore(request.getMinimumScore());
        model.setMaximumScore(request.getMaximumScore());
        model.setEventDate(request.getEventDate());
    }
}

