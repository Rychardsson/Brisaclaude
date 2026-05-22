package com.example.brisa.controllers;

import com.example.brisa.dtos.program.ProgramAddendumRequestDTO;
import com.example.brisa.dtos.program.ProgramAddendumResponseDTO;
import com.example.brisa.services.ProgramAddendumService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/programs/{programId}/addendums")
@RequiredArgsConstructor
public class ProgramAddendumController {

    private final ProgramAddendumService programAddendumService;

    @GetMapping
    public ResponseEntity<List<ProgramAddendumResponseDTO>> list(@PathVariable Long programId) {
        return ResponseEntity.ok(programAddendumService.listByProgram(programId));
    }

    @PostMapping
    public ResponseEntity<ProgramAddendumResponseDTO> create(
            @PathVariable Long programId,
            @RequestBody ProgramAddendumRequestDTO request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(programAddendumService.create(programId, request));
    }

    @PutMapping("/{addendumId}")
    public ResponseEntity<ProgramAddendumResponseDTO> update(
            @PathVariable Long programId,
            @PathVariable Long addendumId,
            @RequestBody ProgramAddendumRequestDTO request
    ) {
        return ResponseEntity.ok(programAddendumService.update(programId, addendumId, request));
    }

    @DeleteMapping("/{addendumId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long programId,
            @PathVariable Long addendumId
    ) {
        programAddendumService.delete(programId, addendumId);
        return ResponseEntity.noContent().build();
    }
}

