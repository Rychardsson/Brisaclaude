package com.example.brisa.controllers;

import com.example.brisa.dtos.imports.SpreadsheetImportResponseDTO;
import com.example.brisa.dtos.institution.InstitutionRequestDTO;
import com.example.brisa.dtos.institution.InstitutionResponseDTO;
import com.example.brisa.services.InstitutionService;
import com.example.brisa.services.LogHelper;
import com.example.brisa.services.SpreadsheetImportService;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping("/api/institutions")
public class InstitutionController {

    @Autowired
    private InstitutionService institutionService;
    
    @Autowired
    private LogHelper logHelper;

    @Autowired
    private SpreadsheetImportService spreadsheetImportService;

    @GetMapping
    public ResponseEntity<List<InstitutionResponseDTO>> getAllInstitutions() {
        List<InstitutionResponseDTO> institutions = institutionService.getAllInstitutions();
        return ResponseEntity.ok(institutions);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InstitutionResponseDTO> getInstitutionById(@PathVariable Long id) {
        InstitutionResponseDTO institution = institutionService.getInstitutionById(id);
        return ResponseEntity.ok(institution);
    }

    @PostMapping("/import/excel")
    public ResponseEntity<SpreadsheetImportResponseDTO> importInstitutionsFromExcel(
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
            SpreadsheetImportResponseDTO response = spreadsheetImportService.importInstitutionsFromExcel(file);

            try {
                UUID userId = getUserId();
                logHelper.logImport("Institution",
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
    public ResponseEntity<InstitutionResponseDTO> createInstitution(
            @RequestBody InstitutionRequestDTO dto,
            HttpServletRequest request) {
        InstitutionResponseDTO created = institutionService.createInstitution(dto);
        
        try {
            UUID userId = getUserId();
            logHelper.logCreate("Institution", created.getId().toString(), 
                created.getName(), userId, request);
        } catch (Exception e) {
            // Log error but don't fail the request
        }
        
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<InstitutionResponseDTO> updateInstitution(
            @PathVariable Long id,
            @RequestBody InstitutionRequestDTO dto,
            HttpServletRequest request) {
        InstitutionResponseDTO updated = institutionService.updateInstitution(id, dto);
        
        try {
            UUID userId = getUserId();
            logHelper.logUpdate("Institution", updated.getId().toString(), 
                updated.getName(), userId, request);
        } catch (Exception e) {
            // Log error but don't fail the request
        }
        
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInstitution(
            @PathVariable Long id,
            HttpServletRequest request) {
        try {
            InstitutionResponseDTO institution = institutionService.getInstitutionById(id);
            UUID userId = getUserId();
            logHelper.logDelete("Institution", id.toString(), institution.getName(), userId, request);
        } catch (Exception e) {
            // Log error but don't fail the request
        }
        
        institutionService.deleteInstitution(id);
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
