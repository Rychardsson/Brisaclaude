package com.example.brisa.controllers;

import com.example.brisa.dtos.career.CareerFollowUpResponseDTO;
import com.example.brisa.dtos.career.CareerPublicAccessResponseDTO;
import com.example.brisa.dtos.career.CareerPublicAccessValidationRequestDTO;
import com.example.brisa.dtos.career.CareerPublicFollowUpSubmissionRequestDTO;
import com.example.brisa.services.CareerService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/public/career")
@RequiredArgsConstructor
public class PublicCareerController {

    private final CareerService careerService;

    @PostMapping("/validate")
    public ResponseEntity<CareerPublicAccessResponseDTO> validateAccess(
            @RequestBody CareerPublicAccessValidationRequestDTO request
    ) {
        return ResponseEntity.ok(careerService.validatePublicAccess(request));
    }

    @PostMapping("/follow-ups")
    public ResponseEntity<CareerFollowUpResponseDTO> submitFollowUp(
            @RequestBody CareerPublicFollowUpSubmissionRequestDTO request,
            HttpServletRequest httpRequest
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(careerService.createPublicFollowUp(request, httpRequest));
    }
}
