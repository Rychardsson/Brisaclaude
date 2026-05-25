package com.example.brisa.controllers;

import com.example.brisa.dtos.course.CourseAlertRequestDTO;
import com.example.brisa.dtos.course.CourseAlertResponseDTO;
import com.example.brisa.dtos.course.CourseClassImportResponseDTO;
import com.example.brisa.dtos.course.CourseProgressionImportResponseDTO;
import com.example.brisa.models.course.CourseModel;
import com.example.brisa.dtos.course.CourseImportDTO;
import org.springframework.http.HttpStatus;
import com.example.brisa.models.course.CourseProgressionModel;
import com.example.brisa.services.CourseService;
import com.example.brisa.models.course.CourseAssignmentModel;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    // GET /api/courses
    @GetMapping
    public ResponseEntity<List<CourseModel>> getAllCourses() {
        return ResponseEntity.ok(courseService.findAll());
    }

    // GET /api/courses/{id}
    @GetMapping("/{id}")
    public ResponseEntity<CourseModel> getCourseById(@PathVariable Long id) {
        return ResponseEntity.ok(courseService.findById(id));
    }

    // GET /api/courses/progressions/class/{classId}
    @GetMapping("/progressions/class/{classId}")
    public ResponseEntity<List<CourseProgressionModel>> getProgressionsByClassId(@PathVariable Long classId) {
        return ResponseEntity.ok(courseService.findProgressionsByClassId(classId));
    }

    // GET /api/courses/backfill?classId={classId}
    @GetMapping("/backfill")
    public ResponseEntity<List<CourseModel>> getBackfillCourses(@RequestParam Long classId) {
        return ResponseEntity.ok(courseService.findBackfillCourses(classId));
    }

    // POST /api/courses/{courseId}/assign/class/{classId}
    @PostMapping("/{courseId}/assign/class/{classId}")
    public ResponseEntity<Void> assignCourseToClass(
            @PathVariable Long courseId,
            @PathVariable Long classId,
            @RequestParam(name = "required", required = false) Boolean required,
            @RequestParam(name = "advisorId", required = false) Long advisorId) {
        courseService.assignCourseToClass(courseId, classId, required == null ? true : required.booleanValue(), advisorId);
        return ResponseEntity.ok().build();
    }

    // DELETE /api/courses/{courseId}/assign/class/{classId}
    @DeleteMapping("/{courseId}/assign/class/{classId}")
    public ResponseEntity<Void> removeCourseFromClass(
            @PathVariable Long courseId,
            @PathVariable Long classId) {
        courseService.removeCourseFromClass(courseId, classId);
        return ResponseEntity.noContent().build();
    }

    // GET /api/courses/progressions/course/{courseId}
    @GetMapping("/progressions/course/{courseId}")
    public ResponseEntity<List<CourseProgressionModel>> getProgressionsByCourseId(@PathVariable Long courseId) {
        return ResponseEntity.ok(courseService.findProgressionsByCourseId(courseId));
    }

    // GET /api/courses/assignments/class/{classId}
    @GetMapping("/assignments/class/{classId}")
    public ResponseEntity<List<CourseAssignmentModel>> getAssignmentsByClassId(@PathVariable Long classId) {
        return ResponseEntity.ok(courseService.findAssignmentsByClassId(classId));
    }

    // POST /api/courses/assignments/class/{classId}/import/excel
    @PostMapping("/assignments/class/{classId}/import/excel")
    public ResponseEntity<CourseClassImportResponseDTO> importCoursesToClassFromExcel(
            @PathVariable Long classId,
            @RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        String fileName = file.getOriginalFilename();
        if (fileName == null || (!fileName.endsWith(".xlsx") && !fileName.endsWith(".xls"))) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(courseService.importCoursesToClassFromExcel(classId, file));
    }

    // POST /api/courses/progressions/class/{classId}/import/excel
    @PostMapping("/progressions/class/{classId}/import/excel")
    public ResponseEntity<CourseProgressionImportResponseDTO> importProgressionsFromExcel(
            @PathVariable Long classId,
            @RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        String fileName = file.getOriginalFilename();
        if (fileName == null || (!fileName.endsWith(".xlsx") && !fileName.endsWith(".xls"))) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(courseService.importProgressionsFromExcel(classId, file));
    }

    // ✅ POST /api/courses/{courseId}/alert/class/{classId}
    // Envia alerta por email para alunos pendentes (não iniciado / em andamento)
    @PostMapping("/{courseId}/alert/class/{classId}")
    public ResponseEntity<CourseAlertResponseDTO> sendAlert(
            @PathVariable Long courseId,
            @PathVariable Long classId,
            @RequestBody CourseAlertRequestDTO request) {
        return ResponseEntity.ok(courseService.sendCourseAlert(courseId, classId, request));
    }

    // POST /api/courses
    @PostMapping
    public ResponseEntity<CourseModel> createCourse(@RequestBody CourseModel course) {
        CourseModel created = courseService.createCourse(course);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // POST /api/courses/bulk
    @PostMapping("/bulk")
    public ResponseEntity<java.util.List<CourseModel>> importCourses(@RequestBody java.util.List<CourseImportDTO> dtos) {
        java.util.List<CourseModel> created = courseService.importCourses(dtos);
        return ResponseEntity.ok(created);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<CourseModel> updateCourse(@PathVariable Long id, @RequestBody CourseModel course) {
        CourseModel updated = courseService.updateCourse(id, course);
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }
}
