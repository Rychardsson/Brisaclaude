package com.example.brisa.controllers;

import com.example.brisa.models.AdvisorModel;
import com.example.brisa.services.AdvisorService;
import com.example.brisa.services.LogHelper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/advisors")
@RequiredArgsConstructor
public class AdvisorController {

    private final AdvisorService advisorService;
    private final LogHelper logHelper;

    @GetMapping
    public ResponseEntity<List<AdvisorModel>> getAll() {
        return ResponseEntity.ok(advisorService.findAll());
    }

    @PostMapping
    public ResponseEntity<AdvisorModel> createAdvisor(@RequestBody AdvisorModel advisor, HttpServletRequest request) {
        AdvisorModel created = advisorService.create(advisor);
        try {
            UUID userId = getUserId();
            if (userId != null) {
                logHelper.logCreate("Advisor", created.getId().toString(), created.getName(), userId, request);
            }
        } catch (Exception ignored) {
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
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
