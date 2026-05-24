package com.example.brisa.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.brisa.dtos.log.LogStatsDTO;
import com.example.brisa.dtos.log.SystemLogDTO;
import com.example.brisa.dtos.log.SystemLogFilterDTO;
import com.example.brisa.enums.LogAction;
import com.example.brisa.services.SystemLogService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("api/logs")
@CrossOrigin(origins = "*")
@Slf4j
public class SystemLogController {

    @Autowired
    private SystemLogService logService;

    @GetMapping
    public ResponseEntity<Page<SystemLogDTO>> getLogs(
            @RequestParam(required = false) LogAction action,
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "50") Integer size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        try {
            SystemLogFilterDTO filter = new SystemLogFilterDTO();
            filter.setAction(action);
            filter.setUserId(userId);
            filter.setEntityType(entityType);
            filter.setStartDate(startDate);
            filter.setEndDate(endDate);
            filter.setPage(page);
            filter.setSize(size);
            filter.setSortBy(sortBy);
            filter.setSortDirection(sortDirection);
            
            Page<SystemLogDTO> logs = logService.findLogs(filter);
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            log.error("Erro ao buscar logs com filtros", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<SystemLogDTO> getLogById(@PathVariable Long id) {
        try {
            return logService.findById(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/recent")
    public ResponseEntity<List<SystemLogDTO>> getRecentLogs(
            @RequestParam(defaultValue = "20") int limit
    ) {
        try {
            List<SystemLogDTO> logs = logService.findRecentLogs(limit);
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<LogStatsDTO> getLogStats() {
        try {
            LogStatsDTO stats = logService.getLogStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/cleanup")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> cleanupOldLogs(
            @RequestParam(defaultValue = "90") int daysToKeep
    ) {
        try {
            long deletedCount = logService.deleteOldLogs(daysToKeep);
            return ResponseEntity.ok("Logs deletados: " + deletedCount);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao deletar logs: " + e.getMessage());
        }
    }
}
