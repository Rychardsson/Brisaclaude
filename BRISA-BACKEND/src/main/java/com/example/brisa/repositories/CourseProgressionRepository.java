package com.example.brisa.repositories;

import com.example.brisa.models.course.CourseProgressionModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CourseProgressionRepository extends JpaRepository<CourseProgressionModel, Long> {

    List<CourseProgressionModel> findByCourseId(Long courseId);

    List<CourseProgressionModel> findByPeopleId(Long peopleId);

    Optional<CourseProgressionModel> findByCourseIdAndPeopleId(Long courseId, Long peopleId);

    Optional<CourseProgressionModel> findByCourseIdAndPeopleIdAndClassModelId(Long courseId, Long peopleId, Long classId);

    // Busca todas as progressões de cursos dos alunos matriculados em uma turma
    @Query("""
        SELECT cp FROM CourseProgressionModel cp
        WHERE cp.classModel.id = :classId
        OR (cp.classModel IS NULL AND cp.people.id IN (
            SELECT e.people.id FROM EnrollmentModel e WHERE e.classModel.id = :classId
        ))
    """)
    List<CourseProgressionModel> findByClassId(@Param("classId") Long classId);

    // ✅ Novo: Progressões de um curso específico para alunos de uma turma
    @Query("""
        SELECT cp FROM CourseProgressionModel cp
        WHERE cp.course.id = :courseId
        AND (
            cp.classModel.id = :classId
            OR (cp.classModel IS NULL AND cp.people.id IN (
                SELECT e.people.id FROM EnrollmentModel e WHERE e.classModel.id = :classId
            ))
        )
    """)
    List<CourseProgressionModel> findByCourseIdAndClassId(
        @Param("courseId") Long courseId,
        @Param("classId") Long classId
    );

    // Agrega contagem por status (não iniciado / em andamento / concluído) para uma turma
    @Query("""
        SELECT cp.status, COUNT(cp)
        FROM CourseProgressionModel cp
        WHERE cp.classModel.id = :classId
        OR (cp.classModel IS NULL AND cp.people.id IN (
            SELECT e.people.id FROM EnrollmentModel e WHERE e.classModel.id = :classId
        ))
        GROUP BY cp.status
    """)
    List<Object[]> countStatusByClass(@Param("classId") Long classId);

    // Agrega contagem por status apenas para os alunos presentes em uma etapa (stage)
    @Query("""
        SELECT cp.status, COUNT(cp)
        FROM CourseProgressionModel cp
        WHERE cp.people.id IN (
            SELECT sc.people.id FROM StageCandidateModel sc WHERE sc.stage.id = :stageId
        )
        GROUP BY cp.status
    """)
    List<Object[]> countStatusByStage(@Param("stageId") Long stageId);

        @Query("""
                SELECT cp.date
                FROM CourseProgressionModel cp
                WHERE cp.date IS NOT NULL
                    AND LOWER(cp.status) IN ('concluído', 'concluido')
        """)
        List<LocalDate> findCompletionDates();

        @Query("""
                SELECT cp.date
                FROM CourseProgressionModel cp
                WHERE cp.date IS NOT NULL
                    AND LOWER(cp.status) IN ('concluído', 'concluido')
                    AND (
                        cp.classModel.id = :classId
                        OR (cp.classModel IS NULL AND cp.people.id IN (
                            SELECT e.people.id FROM EnrollmentModel e WHERE e.classModel.id = :classId
                        ))
                    )
        """)
        List<LocalDate> findCompletionDatesByClass(@Param("classId") Long classId);

        @Query("""
                SELECT cp.date
                FROM CourseProgressionModel cp
                WHERE cp.date IS NOT NULL
                    AND LOWER(cp.status) IN ('concluído', 'concluido')
                    AND cp.people.id IN (
                        SELECT sc.people.id FROM StageCandidateModel sc WHERE sc.stage.id = :stageId
                    )
        """)
        List<LocalDate> findCompletionDatesByStage(@Param("stageId") Long stageId);
}
