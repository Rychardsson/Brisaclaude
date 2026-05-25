package com.example.brisa.controllers;

import com.example.brisa.dtos.exam.ApprovedRankingRequestDTO;
import com.example.brisa.dtos.exam.ApprovedRankingResponseDTO;
import com.example.brisa.dtos.exam.ExamImportResponseDTO;
import com.example.brisa.dtos.exam.ExamRespondentProfileDTO;
import com.example.brisa.dtos.exam.ExamSummaryDTO;
import com.example.brisa.services.ExamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("api/classes/{classId}/exams")
@RequiredArgsConstructor
public class ExamController {

    private final ExamService examService;

    @PostMapping("/import-results")
    public ResponseEntity<ExamImportResponseDTO> importResults(
            @PathVariable Long classId,
            @RequestParam("file") MultipartFile file
    ) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || (!fileName.endsWith(".xlsx") && !fileName.endsWith(".xls"))) {
            return ResponseEntity.badRequest().build();
        }

        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(examService.importResultsFromExcel(classId, file));
        } catch (IOException exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/summary")
    public ResponseEntity<ExamSummaryDTO> getSummary(@PathVariable Long classId) {
        return ResponseEntity.ok(examService.getLatestSummary(classId));
    }

    @GetMapping("/respondent-profile")
    public ResponseEntity<ExamRespondentProfileDTO> getRespondentProfile(@PathVariable Long classId) {
        return ResponseEntity.ok(examService.getLatestRespondentProfile(classId));
    }

    @PostMapping("/approved-ranking")
    public ResponseEntity<ApprovedRankingResponseDTO> calculateApprovedRanking(
            @PathVariable Long classId,
            @RequestBody ApprovedRankingRequestDTO request
    ) {
        return ResponseEntity.ok(examService.calculateApprovedRanking(classId, request));
    }
}
