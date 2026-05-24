package com.example.brisa.controllers;

import com.example.brisa.dtos.analytics.ClassStatusReportDTO;
import com.example.brisa.dtos.analytics.CourseCompletionTimelineDTO;
import com.example.brisa.dtos.analytics.CourseProgressionFunnelDTO;
import com.example.brisa.services.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    // GET /api/analytics/course-progression-funnel?classId=123&stageId=45 or &stage=IMERSAO
    @GetMapping("/course-progression-funnel")
    public ResponseEntity<List<CourseProgressionFunnelDTO>> getCourseProgressionFunnel(
            @RequestParam Long classId,
            @RequestParam(required = false) Long stageId,
            @RequestParam(required = false) String stage
    ) {
        return ResponseEntity.ok(analyticsService.getCourseProgressionFunnel(classId, stageId, stage));
    }

    @GetMapping("/course-completions")
    public ResponseEntity<List<CourseCompletionTimelineDTO>> getCourseCompletions(
        @RequestParam(required = false) Long classId,
        @RequestParam(required = false) Long stageId,
        @RequestParam(defaultValue = "week") String granularity
    ) {
        return ResponseEntity.ok(analyticsService.getCourseCompletions(classId, stageId, granularity));
    }

    @GetMapping("/class-status")
    public ResponseEntity<ClassStatusReportDTO> getClassStatusReport(@RequestParam Long classId) {
        return ResponseEntity.ok(analyticsService.getClassStatusReport(classId));
    }
}
