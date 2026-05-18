package com.example.brisa.controllers;

import com.example.brisa.dtos.program.ProgramClassCreateRequestDTO;
import com.example.brisa.dtos.program.ProgramClassCreateResponseDTO;
import com.example.brisa.dtos.program.ProgramClassTemplateDTO;
import com.example.brisa.dtos.program.ProgramOverviewResponseDTO;
import com.example.brisa.models.ProgramModel;
import com.example.brisa.services.LogHelper;
import com.example.brisa.services.ProgramIntegrationService;
import com.example.brisa.services.ProgramService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("api/programs")
@RequiredArgsConstructor
public class ProgramController {

    private final ProgramService programService;
    private final ProgramIntegrationService programIntegrationService;
    private final LogHelper logHelper;

    @GetMapping
    public ResponseEntity<List<ProgramModel>> getAllPrograms() {
        return ResponseEntity.ok(programService.findAll());
    }

    @GetMapping("/overview")
    public ResponseEntity<ProgramOverviewResponseDTO> getProgramsOverview(
            @RequestParam(required = false) String tab,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String stage,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String locality
    ) {
        return ResponseEntity.ok(programIntegrationService.getOverview(tab, search, status, stage, year, locality));
    }

    @GetMapping("/class-templates")
    public ResponseEntity<List<ProgramClassTemplateDTO>> getClassTemplates() {
        return ResponseEntity.ok(programIntegrationService.getClassTemplates());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProgramModel> getProgramById(@PathVariable Long id) {
        return ResponseEntity.ok(programService.findById(id));
    }

    @PostMapping("/import/excel")
    public ResponseEntity<Map<String, String>> importProgramsFromExcel() {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
            .body(Map.of("message", "Importacao de programas ainda nao foi integrada no backend."));
    }

    @PostMapping
    public ResponseEntity<ProgramModel> createProgram(
            @RequestBody ProgramModel program,
            HttpServletRequest request
    ) {
        List<String> errors = new ArrayList<>();
        if (program.getContractNumber() == null || program.getContractNumber().trim().isEmpty()) {
            errors.add("Contract number is required");
        }
        if (program.getStartDate() == null) {
            errors.add("Start date is required");
        }
        if (program.getEndDate() == null) {
            errors.add("End date is required");
        }
        if (!errors.isEmpty()) {
            throw new com.example.brisa.exceptions.ValidationException(errors);
        }

        ProgramModel createdProgram = programService.create(program);

        try {
            UUID userId = getUserId();
            logHelper.logCreate("Program", createdProgram.getId().toString(), createdProgram.getName(), userId, request);
        } catch (Exception ignored) {
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(createdProgram);
    }

    @PostMapping("/{programId}/classes")
    public ResponseEntity<ProgramClassCreateResponseDTO> createClassFromProgram(
            @PathVariable Long programId,
            @RequestBody ProgramClassCreateRequestDTO request,
            HttpServletRequest httpRequest
    ) {
        ProgramClassCreateResponseDTO response = programIntegrationService.createClassFromProgram(programId, request);

        try {
            UUID userId = getUserId();
            logHelper.logCreate("Class", response.id().toString(), response.codigo(), userId, httpRequest);
        } catch (Exception ignored) {
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProgramModel> updateProgram(
            @PathVariable Long id,
            @RequestBody ProgramModel program,
            HttpServletRequest request
    ) {
        List<String> errors = new ArrayList<>();
        if (program.getContractNumber() == null || program.getContractNumber().trim().isEmpty()) {
            errors.add("Contract number is required");
        }
        if (program.getStartDate() == null) {
            errors.add("Start date is required");
        }
        if (program.getEndDate() == null) {
            errors.add("End date is required");
        }
        if (!errors.isEmpty()) {
            throw new com.example.brisa.exceptions.ValidationException(errors);
        }

        ProgramModel updatedProgram = programService.update(id, program);

        try {
            UUID userId = getUserId();
            logHelper.logUpdate("Program", updatedProgram.getId().toString(), updatedProgram.getName(), userId, request);
        } catch (Exception ignored) {
        }

        return ResponseEntity.ok(updatedProgram);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProgram(
            @PathVariable Long id,
            HttpServletRequest request
    ) {
        try {
            ProgramModel program = programService.findById(id);
            UUID userId = getUserId();
            logHelper.logDelete("Program", id.toString(), program.getName(), userId, request);
        } catch (Exception ignored) {
        }

        programService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private UUID getUserId() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof com.example.brisa.models.auth.UserModel user) {
                return user.getId();
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}
