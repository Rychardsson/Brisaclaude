package com.example.brisa.services;

import com.example.brisa.dtos.analytics.CourseCompletionTimelineDTO;
import com.example.brisa.dtos.analytics.CourseProgressionFunnelDTO;
import com.example.brisa.dtos.analytics.ClassStatusReportDTO;
import com.example.brisa.models.EnrollmentModel;
import com.example.brisa.models.course.CourseProgressionModel;
import com.example.brisa.repositories.CourseProgressionRepository;
import com.example.brisa.repositories.EnrollmentRepository;
import com.example.brisa.repositories.StageRepository;
import com.example.brisa.models.StageModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.WeekFields;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final CourseProgressionRepository courseProgressionRepository;
    private final StageRepository stageRepository;
    private final EnrollmentRepository enrollmentRepository;

    public List<CourseProgressionFunnelDTO> getCourseProgressionFunnel(Long classId) {
        return getCourseProgressionFunnel(classId, null, null);
    }

    public List<CourseProgressionFunnelDTO> getCourseProgressionFunnel(Long classId, Long stageId, String stageName) {
        List<Object[]> rows;

        if (stageId != null) {
            rows = courseProgressionRepository.countStatusByStage(stageId);
        } else if (stageName != null && classId != null) {
            var optStage = stageRepository.findByNameAndClassModelId(stageName, classId);
            if (optStage.isPresent()) {
                StageModel stage = optStage.get();
                rows = courseProgressionRepository.countStatusByStage(stage.getId());
            } else {
                rows = courseProgressionRepository.countStatusByClass(classId);
            }
        } else {
            rows = courseProgressionRepository.countStatusByClass(classId);
        }

        return rows.stream()
                .map(r -> new CourseProgressionFunnelDTO((String) r[0], ((Number) r[1]).longValue()))
                .collect(Collectors.toList());
    }

    public List<CourseCompletionTimelineDTO> getCourseCompletions(Long classId, Long stageId, String granularity) {
        List<LocalDate> dates;

        if (stageId != null) {
            dates = courseProgressionRepository.findCompletionDatesByStage(stageId);
        } else if (classId != null) {
            dates = courseProgressionRepository.findCompletionDatesByClass(classId);
        } else {
            dates = courseProgressionRepository.findCompletionDates();
        }

        String normalizedGranularity = granularity == null ? "week" : granularity.trim().toLowerCase(Locale.ROOT);
        Map<String, Long> grouped = new TreeMap<>();

        for (LocalDate date : dates) {
            String key = switch (normalizedGranularity) {
                case "day" -> date.toString();
                case "month" -> YearMonth.from(date).toString();
                default -> {
                    WeekFields wf = WeekFields.ISO;
                    int week = date.get(wf.weekOfWeekBasedYear());
                    int year = date.get(wf.weekBasedYear());
                    yield String.format("%d-W%02d", year, week);
                }
            };

            grouped.put(key, grouped.getOrDefault(key, 0L) + 1L);
        }

        return grouped.entrySet().stream()
            .map(e -> new CourseCompletionTimelineDTO(e.getKey(), e.getValue()))
            .collect(Collectors.toList());
    }

    public ClassStatusReportDTO getClassStatusReport(Long classId) {
        List<EnrollmentModel> studentEnrollments = enrollmentRepository.findByClassIdWithRelations(classId).stream()
                .filter(this::isStudentEnrollment)
                .toList();
        List<EnrollmentModel> activeStudentEnrollments = studentEnrollments.stream()
                .filter(enrollment -> isActiveStatus(enrollment.getStatus()))
                .toList();
        List<CourseProgressionModel> progressions = courseProgressionRepository.findByClassId(classId);

        Set<Long> activePeopleIds = activeStudentEnrollments.stream()
                .filter(enrollment -> enrollment.getPeople() != null && enrollment.getPeople().getId() != null)
                .map(enrollment -> enrollment.getPeople().getId())
                .collect(Collectors.toSet());

        Map<Long, Long> completedByPeople = progressions.stream()
                .filter(this::isCompletedProgression)
                .filter(progression -> progression.getPeople() != null && progression.getPeople().getId() != null)
                .filter(progression -> activePeopleIds.contains(progression.getPeople().getId()))
                .collect(Collectors.groupingBy(
                        progression -> progression.getPeople().getId(),
                        Collectors.counting()
                ));

        long activeStudents = activeStudentEnrollments.size();
        long studentsWithProgress = activeStudentEnrollments.stream()
                .filter(enrollment -> completedByPeople.containsKey(enrollment.getPeople().getId()))
                .count();
        long maxCompletedCourses = completedByPeople.values().stream()
                .mapToLong(Long::longValue)
                .max()
                .orElse(0L);

        Map<Integer, Long> buckets = new LinkedHashMap<>();
        for (int completed = 0; completed <= Math.max(1, maxCompletedCourses); completed++) {
            buckets.put(completed, 0L);
        }

        for (EnrollmentModel enrollment : activeStudentEnrollments) {
            Long peopleId = enrollment.getPeople().getId();
            int completedCourses = completedByPeople.getOrDefault(peopleId, 0L).intValue();
            buckets.put(completedCourses, buckets.getOrDefault(completedCourses, 0L) + 1L);
        }

        long totalStudents = activeStudentEnrollments.size();
        List<ClassStatusReportDTO.CompletionBucketDTO> completionBuckets = buckets.entrySet().stream()
                .map(entry -> new ClassStatusReportDTO.CompletionBucketDTO(
                        entry.getKey(),
                        entry.getValue(),
                        totalStudents == 0 ? 0D : roundPercentage((entry.getValue() * 100D) / totalStudents)
                ))
                .toList();

        return new ClassStatusReportDTO(
                classId,
                totalStudents,
                activeStudents,
                studentsWithProgress,
                Math.max(0, totalStudents - studentsWithProgress),
                maxCompletedCourses,
                completionBuckets
        );
    }

    private boolean isStudentEnrollment(EnrollmentModel enrollment) {
        return enrollment.getAcademicRole() == null
                || enrollment.getAcademicRole().getName() == null
                || "ALUNO".equalsIgnoreCase(enrollment.getAcademicRole().getName());
    }

    private boolean isCompletedProgression(CourseProgressionModel progression) {
        String status = progression.getStatus() == null ? "" : progression.getStatus().trim().toLowerCase(Locale.ROOT);
        return progression.getCompletionPercentage() >= 100D
                || status.contains("concluido")
                || status.contains("conclu");
    }

    private boolean isActiveStatus(String status) {
        String normalized = status == null ? "" : status.trim().toLowerCase(Locale.ROOT);
        return normalized.isBlank()
                || normalized.contains("ativa")
                || normalized.contains("ativo")
                || normalized.contains("andamento")
                || normalized.contains("pendente");
    }

    private double roundPercentage(double value) {
        return Math.round(value * 10D) / 10D;
    }
}
