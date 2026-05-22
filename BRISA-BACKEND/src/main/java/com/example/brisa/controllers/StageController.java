package com.example.brisa.controllers;

import com.example.brisa.dtos.stage.*;
import com.example.brisa.services.StageService;
import com.example.brisa.services.LogHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("api/stages")
@RequiredArgsConstructor
public class StageController {
    
    private final StageService stageService;
    private final LogHelper logHelper;

    // Stage Endpoints

    @GetMapping
    public ResponseEntity<List<StageResponseDTO>> getAllStages() {
        List<StageResponseDTO> stages = stageService.findAll();
        return ResponseEntity.ok(stages);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StageResponseDTO> getStageById(@PathVariable Long id) {
        StageResponseDTO stage = stageService.findById(id);
        return ResponseEntity.ok(stage);
    }

    @GetMapping("/class/{classId}")
    public ResponseEntity<List<StageResponseDTO>> getStagesByClassId(@PathVariable Long classId) {
        List<StageResponseDTO> stages = stageService.findByClassId(classId);
        return ResponseEntity.ok(stages);
    }

    @GetMapping("/class/{classId}/candidates/count")
    public ResponseEntity<Map<Long, Long>> getCandidatesCountByClassId(@PathVariable Long classId) {
        Map<Long, Long> counts = stageService.getCandidatesCountByClassId(classId);
        return ResponseEntity.ok(counts);
    }

    @PostMapping
    public ResponseEntity<StageResponseDTO> createStage(
            @RequestBody StageRequestDTO stageRequestDTO,
            HttpServletRequest request) {
        StageResponseDTO createdStage = stageService.create(stageRequestDTO);
        
        try {
            UUID userId = getUserId();
            logHelper.logCreate("Stage", createdStage.getId().toString(), 
                createdStage.getName(), userId, request);
        } catch (Exception e) {
            // Log error but don't fail the request
        }
        
        return ResponseEntity.status(HttpStatus.CREATED).body(createdStage);
    }

    @PutMapping("/{id}")
    public ResponseEntity<StageResponseDTO> updateStage(
            @PathVariable Long id,
            @RequestBody StageRequestDTO stageRequestDTO,
            HttpServletRequest request) {
        StageResponseDTO updatedStage = stageService.update(id, stageRequestDTO);
        
        try {
            UUID userId = getUserId();
            logHelper.logUpdate("Stage", updatedStage.getId().toString(), 
                updatedStage.getName(), userId, request);
        } catch (Exception e) {
            // Log error but don't fail the request
        }
        
        return ResponseEntity.ok(updatedStage);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStage(
            @PathVariable Long id,
            HttpServletRequest request) {
        try {
            StageResponseDTO stage = stageService.findById(id);
            UUID userId = getUserId();
            logHelper.logDelete("Stage", id.toString(), stage.getName(), userId, request);
        } catch (Exception e) {
            // Log error but don't fail the request
        }
        
        stageService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // Stage Candidate Endpoints

    @GetMapping("/candidates")
    public ResponseEntity<List<StageCandidateResponseDTO>> getAllCandidates() {
        List<StageCandidateResponseDTO> candidates = stageService.findAllCandidates();
        return ResponseEntity.ok(candidates);
    }

    @GetMapping("/candidates/{id}")
    public ResponseEntity<StageCandidateResponseDTO> getCandidateById(@PathVariable Long id) {
        StageCandidateResponseDTO candidate = stageService.findCandidateById(id);
        return ResponseEntity.ok(candidate);
    }

    @GetMapping("/{stageId}/candidates")
    public ResponseEntity<List<StageCandidateResponseDTO>> getCandidatesByStageId(@PathVariable Long stageId) {
        List<StageCandidateResponseDTO> candidates = stageService.findCandidatesByStageId(stageId);
        return ResponseEntity.ok(candidates);
    }

    @GetMapping("/people/{peopleId}/candidates")
    public ResponseEntity<List<StageCandidateResponseDTO>> getCandidatesByPeopleId(@PathVariable Long peopleId) {
        List<StageCandidateResponseDTO> candidates = stageService.findCandidatesByPeopleId(peopleId);
        return ResponseEntity.ok(candidates);
    }

    @PostMapping("/candidates")
    public ResponseEntity<StageCandidateResponseDTO> addCandidate(
            @RequestBody StageCandidateRequestDTO requestDTO,
            HttpServletRequest request) {
        StageCandidateResponseDTO createdCandidate = stageService.addCandidate(requestDTO);
        
        try {
            UUID userId = getUserId();
            logHelper.logAsync(com.example.brisa.enums.LogAction.STAGE_CANDIDATE_ADD,
                "Candidato " + createdCandidate.getPeopleName() + " adicionado à etapa",
                "StageCandidate", createdCandidate.getId().toString(), userId, request);
        } catch (Exception e) {
            // Log error but don't fail the request
        }
        
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCandidate);
    }

    @PutMapping("/candidates/{id}")
    public ResponseEntity<StageCandidateResponseDTO> updateCandidate(
            @PathVariable Long id,
            @RequestBody StageCandidateRequestDTO requestDTO) {
        StageCandidateResponseDTO updatedCandidate = stageService.updateCandidate(id, requestDTO);
        return ResponseEntity.ok(updatedCandidate);
    }

    @DeleteMapping("/candidates/{id}")
    public ResponseEntity<Void> deleteCandidate(
            @PathVariable Long id,
            HttpServletRequest request) {
        try {
            StageCandidateResponseDTO candidate = stageService.findCandidateById(id);
            UUID userId = getUserId();
            logHelper.logAsync(com.example.brisa.enums.LogAction.STAGE_CANDIDATE_REMOVE,
                "Candidato " + candidate.getPeopleName() + " removido da etapa",
                "StageCandidate", id.toString(), userId, request);
        } catch (Exception e) {
            // Log error but don't fail the request
        }
        
        stageService.deleteCandidate(id);
        return ResponseEntity.noContent().build();
    }

    // Import Candidates from Excel

    @PostMapping("/{stageId}/candidates/import/excel")
    public ResponseEntity<CandidateImportResponseDTO> importCandidatesFromExcel(
            @PathVariable Long stageId,
            @RequestParam("file") MultipartFile file) {

        // Valida se o arquivo foi enviado
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        // Valida se é um arquivo Excel
        String fileName = file.getOriginalFilename();
        if (fileName == null || (!fileName.endsWith(".xlsx") && !fileName.endsWith(".xls"))) {
            return ResponseEntity.badRequest().build();
        }

        try {
            CandidateImportResponseDTO response = stageService.importCandidatesFromExcel(stageId, file);
            
            try {
                UUID userId = getUserId();
                logHelper.logImport("StageCandidate", 
                    response.getSuccessfullyInserted(),
                    response.getTotalProcessed() - response.getSuccessfullyInserted(),
                    userId, null);
            } catch (Exception e) {
                // Log error but don't fail the request
            }
            
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{stageId}/candidates/import/approved")
    public ResponseEntity<ApprovedImportResponseDTO> importApprovedCandidatesFromExcel(
            @PathVariable Long stageId,
            @RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || (!fileName.endsWith(".xlsx") && !fileName.endsWith(".xls"))) {
            return ResponseEntity.badRequest().build();
        }

        try {
            return ResponseEntity.ok(stageService.importApprovedCandidatesFromExcel(stageId, file));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{stageId}/waitlist/convoke")
    public ResponseEntity<WaitlistConvokeResponseDTO> convokeWaitlist(
            @PathVariable Long stageId,
            @RequestBody WaitlistConvokeRequestDTO requestDTO
    ) {
        return ResponseEntity.ok(stageService.convokeWaitlist(stageId, requestDTO));
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
