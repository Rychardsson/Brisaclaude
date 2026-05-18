package com.example.brisa.controllers;

import com.example.brisa.models.ClassModel;
import com.example.brisa.services.ClassService;
import com.example.brisa.services.LogHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("api/classes")
@RequiredArgsConstructor
public class ClassController {
    
    private final ClassService classService;
    private final LogHelper logHelper;

    @GetMapping
    public ResponseEntity<List<ClassModel>> getAllClasses() {
        return ResponseEntity.ok(classService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClassModel> getClassById(@PathVariable Long id) {
        return ResponseEntity.ok(classService.findById(id));
    }

    @GetMapping("/program/{programId}")
    public ResponseEntity<List<ClassModel>> getClassesByProgramId(@PathVariable Long programId) {
        return ResponseEntity.ok(classService.findByProgramId(programId));
    }

    // ✅ GET /api/classes/count-by-program
    // Retorna { programId: count } em uma única query — usado pela ProgramsView para exibir contagem de turmas
    @GetMapping("/count-by-program")
    public ResponseEntity<Map<Long, Long>> getClassesCountByProgram() {
        return ResponseEntity.ok(classService.getClassesCountByProgram());
    }

    @PostMapping("/import/excel")
    public ResponseEntity<Map<String, String>> importClassesFromExcel() {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
            .body(Map.of("message", "Importacao de turmas ainda nao foi integrada no backend."));
    }

    @PostMapping
    public ResponseEntity<ClassModel> createClass(
            @RequestBody ClassModel classModel,
            HttpServletRequest request) {
        ClassModel createdClass = classService.create(classModel);
        try {
            UUID userId = getUserId();
            logHelper.logCreate("Class", createdClass.getId().toString(),
                createdClass.getCode(), userId, request);
        } catch (Exception e) { }
        return ResponseEntity.status(HttpStatus.CREATED).body(createdClass);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClassModel> updateClass(
            @PathVariable Long id,
            @RequestBody ClassModel classModel,
            HttpServletRequest request) {
        ClassModel updatedClass = classService.update(id, classModel);
        try {
            UUID userId = getUserId();
            logHelper.logUpdate("Class", updatedClass.getId().toString(),
                updatedClass.getCode(), userId, request);
        } catch (Exception e) { }
        return ResponseEntity.ok(updatedClass);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClass(
            @PathVariable Long id,
            HttpServletRequest request) {
        try {
            ClassModel classModel = classService.findById(id);
            UUID userId = getUserId();
            logHelper.logDelete("Class", id.toString(), classModel.getCode(), userId, request);
        } catch (Exception e) { }
        classService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private UUID getUserId() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof com.example.brisa.models.auth.UserModel) {
                return ((com.example.brisa.models.auth.UserModel) auth.getPrincipal()).getId();
            }
        } catch (Exception e) { }
        return null;
    }
}
