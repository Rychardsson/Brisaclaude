package com.example.brisa.controllers;

import com.example.brisa.dtos.career.CareerAutomationSettingsRequestDTO;
import com.example.brisa.dtos.career.CareerAutomationSettingsResponseDTO;
import com.example.brisa.dtos.career.CareerFollowUpRequestDTO;
import com.example.brisa.dtos.career.CareerFollowUpResponseDTO;
import com.example.brisa.models.auth.UserModel;
import com.example.brisa.services.CareerService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("api/career")
@RequiredArgsConstructor
public class CareerController {

    private final CareerService careerService;

    @GetMapping("/follow-ups")
    public ResponseEntity<List<CareerFollowUpResponseDTO>> getFollowUps(
            @RequestParam(required = false) Long peopleId,
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) Long programId
    ) {
        return ResponseEntity.ok(careerService.getFollowUps(peopleId, classId, programId));
    }

    @PostMapping("/follow-ups")
    public ResponseEntity<CareerFollowUpResponseDTO> createFollowUp(
            @RequestBody CareerFollowUpRequestDTO request,
            HttpServletRequest httpRequest
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(careerService.createFollowUp(request, getUserId(), httpRequest));
    }

    @GetMapping("/automation")
    public ResponseEntity<CareerAutomationSettingsResponseDTO> getAutomationSettings() {
        return ResponseEntity.ok(careerService.getAutomationSettings());
    }

    @PutMapping("/automation")
    public ResponseEntity<CareerAutomationSettingsResponseDTO> updateAutomationSettings(
            @RequestBody CareerAutomationSettingsRequestDTO request,
            HttpServletRequest httpRequest
    ) {
        return ResponseEntity.ok(careerService.updateAutomationSettings(request, getUserId(), httpRequest));
    }

    @PostMapping("/automation/test")
    public ResponseEntity<Map<String, Object>> registerAutomationTest(
            @RequestBody CareerAutomationSettingsRequestDTO request,
            HttpServletRequest httpRequest
    ) {
        CareerAutomationSettingsResponseDTO response = careerService.registerAutomationTest(request, getUserId(), httpRequest);
        return ResponseEntity.ok(Map.of(
                "message", "Teste de automacao registrado com sucesso.",
                "settings", response
        ));
    }

    private UUID getUserId() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof UserModel user) {
                return user.getId();
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}
