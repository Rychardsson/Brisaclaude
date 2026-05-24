package com.example.brisa.dtos.analytics;

import java.util.List;

public record ClassStatusReportDTO(
        Long classId,
        long totalStudents,
        long activeStudents,
        long studentsWithProgress,
        long studentsWithoutProgress,
        long maxCompletedCourses,
        List<CompletionBucketDTO> completionBuckets
) {
    public record CompletionBucketDTO(
            int completedCourses,
            long students,
            double percentage
    ) {
    }
}
