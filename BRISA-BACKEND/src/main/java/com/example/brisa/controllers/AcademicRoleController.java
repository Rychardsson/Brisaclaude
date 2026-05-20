package com.example.brisa.controllers;

import com.example.brisa.models.roles.AcademicRoleModel;
import com.example.brisa.repositories.AcademicRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("api/academic-roles")
@RequiredArgsConstructor
public class AcademicRoleController {

    private final AcademicRoleRepository academicRoleRepository;

    @GetMapping
    public ResponseEntity<List<AcademicRoleModel>> getAllAcademicRoles() {
        List<AcademicRoleModel> roles = academicRoleRepository.findAll().stream()
                .sorted(Comparator.comparing(AcademicRoleModel::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();
        return ResponseEntity.ok(roles);
    }
}
