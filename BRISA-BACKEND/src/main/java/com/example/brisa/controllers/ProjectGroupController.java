package com.example.brisa.controllers;

import com.example.brisa.dtos.ProjectGroupCreateRequestDTO;
import com.example.brisa.dtos.ProjectGroupDetailResponseDTO;
import com.example.brisa.dtos.ProjectGroupResponseDTO;
import com.example.brisa.models.PeopleModel;
import com.example.brisa.services.ProjectGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/classes/{classId}")
@RequiredArgsConstructor
public class ProjectGroupController {

    private final ProjectGroupService projectGroupService;

    // Endpoint para criar grupo
    @PostMapping("/groups")
    public ResponseEntity<?> createProjectGroup(
            @PathVariable Long classId,
            @RequestBody ProjectGroupCreateRequestDTO requestDTO) {
        try {
            ProjectGroupResponseDTO response = projectGroupService.createProjectGroup(classId, requestDTO);
            Map<String, Object> result = new HashMap<>();
            result.put("message", "Grupo criado com sucesso");
            result.put("group", response);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    // Endpoint para listar grupos da turma
    @GetMapping("/groups")
    public ResponseEntity<List<ProjectGroupResponseDTO>> getGroupsByClass(
            @PathVariable Long classId) {
        List<ProjectGroupResponseDTO> groups = projectGroupService.getGroupsByClass(classId);
        return ResponseEntity.ok(groups);
    }

    // Endpoint para obter detalhe do grupo
    @GetMapping("/groups/{groupId}")
    public ResponseEntity<ProjectGroupDetailResponseDTO> getGroupDetail(
            @PathVariable Long classId,
            @PathVariable Long groupId) {
        ProjectGroupDetailResponseDTO group = projectGroupService.getGroupDetail(groupId);
        return ResponseEntity.ok(group);
    }

    // Endpoint para atualizar grupo
    @PutMapping("/groups/{groupId}")
    public ResponseEntity<?> updateProjectGroup(
            @PathVariable Long classId,
            @PathVariable Long groupId,
            @RequestBody ProjectGroupCreateRequestDTO requestDTO) {
        try {
            ProjectGroupResponseDTO response = projectGroupService.updateProjectGroup(groupId, requestDTO);
            Map<String, Object> result = new HashMap<>();
            result.put("message", "Grupo atualizado com sucesso");
            result.put("group", response);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    // Endpoint para deletar grupo
    @DeleteMapping("/groups/{groupId}")
    public ResponseEntity<?> deleteProjectGroup(
            @PathVariable Long classId,
            @PathVariable Long groupId) {
        try {
            projectGroupService.deleteProjectGroup(groupId);
            Map<String, String> result = new HashMap<>();
            result.put("message", "Grupo deletado com sucesso");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    // Endpoint para obter alunos da etapa de imersão
    @GetMapping("/immersion-students")
    public ResponseEntity<?> getImmersionStudents(@PathVariable Long classId) {
        try {
            List<PeopleModel> availableStudents = projectGroupService.getAvailableImmersionStudents(classId);

            List<Map<String, Object>> students = availableStudents.stream()
                    .map(people -> {
                        Map<String, Object> student = new HashMap<>();
                        student.put("id", people.getId());
                        student.put("name", people.getName());
                        student.put("email", people.getEmail());
                        return student;
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(students);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
}

