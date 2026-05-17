package com.example.brisa.controllers;

import com.example.brisa.dtos.imports.SpreadsheetImportResponseDTO;
import com.example.brisa.models.EnrollmentModel;
import com.example.brisa.services.EnrollmentService;
import com.example.brisa.services.LogHelper;
import com.example.brisa.services.SpreadsheetImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {
    
    private final EnrollmentService enrollmentService;
    private final SpreadsheetImportService spreadsheetImportService;
    private final LogHelper logHelper;

    @GetMapping
    public ResponseEntity<List<EnrollmentModel>> getAllEnrollments() {
        List<EnrollmentModel> enrollments = enrollmentService.findAll();
        return ResponseEntity.ok(enrollments);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EnrollmentModel> getEnrollmentById(@PathVariable Long id) {
        EnrollmentModel enrollment = enrollmentService.findById(id);
        return ResponseEntity.ok(enrollment);
    }

    @GetMapping("/class/{classId}")
    public ResponseEntity<List<EnrollmentModel>> getEnrollmentsByClassId(@PathVariable Long classId) {
        List<EnrollmentModel> enrollments = enrollmentService.findByClassId(classId);
        return ResponseEntity.ok(enrollments);
    }

    @GetMapping("/people/{peopleId}")
    public ResponseEntity<List<EnrollmentModel>> getEnrollmentsByPeopleId(@PathVariable Long peopleId) {
        List<EnrollmentModel> enrollments = enrollmentService.findByPeopleId(peopleId);
        return ResponseEntity.ok(enrollments);
    }

    @PostMapping("/import/excel")
    public ResponseEntity<SpreadsheetImportResponseDTO> importEnrollmentsFromExcel(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request
    ) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || (!fileName.endsWith(".xlsx") && !fileName.endsWith(".xls"))) {
            return ResponseEntity.badRequest().build();
        }

        try {
            SpreadsheetImportResponseDTO response = spreadsheetImportService.importEnrollmentsFromExcel(file);

            try {
                UUID userId = getUserId();
                logHelper.logImport("Enrollment",
                        response.getSuccessfullyInserted() + response.getUpdated(),
                        response.getAlreadyExists() + response.getErrors().size(),
                        userId,
                        request);
            } catch (Exception ignored) {
            }

            return ResponseEntity.ok(response);
        } catch (Exception ignored) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<EnrollmentModel> createEnrollment(
            @RequestBody EnrollmentModel enrollment,
            HttpServletRequest request) {
        EnrollmentModel createdEnrollment = enrollmentService.create(enrollment);
        
        try {
            UUID userId = getUserId();
            String entityName = "Matrícula: " + createdEnrollment.getPeople().getName() + 
                " -> " + createdEnrollment.getClassModel().getCode();
            logHelper.logCreate("Enrollment", createdEnrollment.getId().toString(), 
                entityName, userId, request);
        } catch (Exception e) {
            // Log error but don't fail the request
        }
        
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEnrollment);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EnrollmentModel> updateEnrollment(
            @PathVariable Long id,
            @RequestBody EnrollmentModel enrollment,
            HttpServletRequest request) {
        EnrollmentModel updatedEnrollment = enrollmentService.update(id, enrollment);
        
        try {
            UUID userId = getUserId();
            String entityName = "Matrícula: " + updatedEnrollment.getPeople().getName() + 
                " -> " + updatedEnrollment.getClassModel().getCode();
            logHelper.logUpdate("Enrollment", updatedEnrollment.getId().toString(), 
                entityName, userId, request);
        } catch (Exception e) {
            // Log error but don't fail the request
        }
        
        return ResponseEntity.ok(updatedEnrollment);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEnrollment(
            @PathVariable Long id,
            HttpServletRequest request) {
        try {
            EnrollmentModel enrollment = enrollmentService.findById(id);
            UUID userId = getUserId();
            String entityName = "Matrícula: " + enrollment.getPeople().getName() + 
                " -> " + enrollment.getClassModel().getCode();
            logHelper.logDelete("Enrollment", id.toString(), entityName, userId, request);
        } catch (Exception e) {
            // Log error but don't fail the request
        }
        
        enrollmentService.delete(id);
        return ResponseEntity.noContent().build();
    }
    
    private UUID getUserId() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof com.example.brisa.models.auth.UserModel) {
                return ((com.example.brisa.models.auth.UserModel) auth.getPrincipal()).getId();
            }
        } catch (Exception e) {
            // Return null if can't get user
        }
        return null;
    }
}
