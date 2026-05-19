package com.example.brisa.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.brisa.dtos.log.LogStatsDTO;
import com.example.brisa.dtos.log.SystemLogDTO;
import com.example.brisa.dtos.log.SystemLogFilterDTO;
import com.example.brisa.enums.LogAction;
import com.example.brisa.models.SystemLogModel;
import com.example.brisa.models.auth.UserModel;
import com.example.brisa.repositories.SystemLogRepository;
import com.example.brisa.repositories.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SystemLogService {

    @Autowired
    private SystemLogRepository logRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Registra um log no sistema
     */
    @Transactional
    public SystemLogModel createLog(
            LogAction action,
            String description,
            String entityType,
            String entityId,
            UUID userId,
            HttpServletRequest request,
            Map<String, Object> additionalDetails
    ) {
        SystemLogModel log = new SystemLogModel();
        log.setAction(action);
        log.setDescription(description);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        
        if (userId != null) {
            userRepository.findById(userId).ifPresent(log::setUser);
        }
        
        if (request != null) {
            log.setIpAddress(getClientIp(request));
            log.setUserAgent(request.getHeader("User-Agent"));
        }
        
        if (additionalDetails != null && !additionalDetails.isEmpty()) {
            try {
                log.setDetails(objectMapper.writeValueAsString(additionalDetails));
            } catch (Exception e) {
                log.setDetails("Error serializing details: " + e.getMessage());
            }
        }
        
        return logRepository.save(log);
    }

    /**
     * Método simplificado para criar log
     */
    @Transactional
    public SystemLogModel createLog(LogAction action, String description, UUID userId) {
        return createLog(action, description, null, null, userId, null, null);
    }

    /**
     * Registra um log com dados do request já extraídos (IP e User-Agent como strings)
     * Evita problemas com request reciclado em operações assíncronas
     */
    @Transactional
    public SystemLogModel createLogWithRequestData(
            LogAction action,
            String description,
            String entityType,
            String entityId,
            UUID userId,
            String ipAddress,
            String userAgent,
            Map<String, Object> additionalDetails
    ) {
        SystemLogModel log = new SystemLogModel();
        log.setAction(action);
        log.setDescription(description);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        
        if (userId != null) {
            userRepository.findById(userId).ifPresent(log::setUser);
        }
        
        if (ipAddress != null && !ipAddress.isEmpty()) {
            log.setIpAddress(ipAddress);
        }
        if (userAgent != null && !userAgent.isEmpty()) {
            log.setUserAgent(userAgent);
        }
        
        if (additionalDetails != null && !additionalDetails.isEmpty()) {
            try {
                log.setDetails(objectMapper.writeValueAsString(additionalDetails));
            } catch (Exception e) {
                log.setDetails("Error serializing details: " + e.getMessage());
            }
        }
        
        return logRepository.save(log);
    }

    /**
     * Buscar logs com filtros
     */
    public Page<SystemLogDTO> findLogs(SystemLogFilterDTO filter) {
        Sort sort = Sort.by(
            filter.getSortDirection().equalsIgnoreCase("ASC") 
                ? Sort.Direction.ASC 
                : Sort.Direction.DESC,
            filter.getSortBy()
        );
        
        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(), sort);
        
        Page<SystemLogModel> logs;
        
        // Se não há filtros, busca todos
        if (filter.getAction() == null && filter.getUserId() == null && 
            filter.getEntityType() == null && filter.getStartDate() == null && 
            filter.getEndDate() == null) {
            logs = logRepository.findAll(pageable);
        }
        // Se tem filtro de data
        else if (filter.getStartDate() != null && filter.getEndDate() != null) {
            logs = logRepository.findByCreatedAtBetween(filter.getStartDate(), filter.getEndDate(), pageable);
        }
        // Se tem filtro de ação
        else if (filter.getAction() != null) {
            logs = logRepository.findByAction(filter.getAction(), pageable);
        }
        // Se tem filtro de tipo de entidade
        else if (filter.getEntityType() != null && !filter.getEntityType().isEmpty()) {
            logs = logRepository.findByEntityType(filter.getEntityType(), pageable);
        }
        // Caso contrário, busca todos
        else {
            logs = logRepository.findAll(pageable);
        }
        
        return logs.map(this::convertToDTO);
    }

    /**
     * Buscar log por ID
     */
    public Optional<SystemLogDTO> findById(Long id) {
        return logRepository.findById(id).map(this::convertToDTO);
    }

    /**
     * Buscar últimos logs
     */
    public List<SystemLogDTO> findRecentLogs(int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        return logRepository.findAll(pageable).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obter estatísticas de logs
     */
    public LogStatsDTO getLogStats() {
        LogStatsDTO stats = new LogStatsDTO();
        
        // Total de logs
        stats.setTotalLogs(logRepository.count());
        
        // Logs por ação
        List<Object[]> actionStats = logRepository.countByActionGrouped();
        Map<LogAction, Long> logsByAction = new HashMap<>();
        for (Object[] stat : actionStats) {
            logsByAction.put((LogAction) stat[0], (Long) stat[1]);
        }
        stats.setLogsByAction(logsByAction);
        
        // Logs de hoje
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        Pageable pageable = PageRequest.of(0, 1);
        stats.setLogsToday(logRepository.findByCreatedAtBetween(startOfDay, endOfDay, pageable).getTotalElements());
        
        // Logs desta semana
        LocalDateTime startOfWeek = LocalDateTime.now().minusDays(7);
        stats.setLogsThisWeek(logRepository.findByCreatedAtBetween(startOfWeek, LocalDateTime.now(), pageable).getTotalElements());
        
        // Logs deste mês
        LocalDateTime startOfMonth = LocalDateTime.now().minusDays(30);
        stats.setLogsThisMonth(logRepository.findByCreatedAtBetween(startOfMonth, LocalDateTime.now(), pageable).getTotalElements());
        
        return stats;
    }

    /**
     * Deletar logs antigos (manutenção)
     */
    @Transactional
    public long deleteOldLogs(int daysToKeep) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE);
        Page<SystemLogModel> oldLogs = logRepository.findByCreatedAtBetween(
            LocalDateTime.of(2000, 1, 1, 0, 0),
            cutoffDate,
            pageable
        );
        
        long count = oldLogs.getTotalElements();
        logRepository.deleteAll(oldLogs.getContent());
        return count;
    }

    /**
     * Converter modelo para DTO
     */
    private SystemLogDTO convertToDTO(SystemLogModel log) {
        SystemLogDTO dto = new SystemLogDTO();
        dto.setId(log.getId());
        dto.setAction(log.getAction());
        dto.setDescription(log.getDescription());
        dto.setEntityType(log.getEntityType());
        dto.setEntityId(log.getEntityId());
        dto.setIpAddress(log.getIpAddress());
        dto.setUserAgent(log.getUserAgent());
        dto.setDetails(log.getDetails());
        dto.setCreatedAt(log.getCreatedAt());
        
        if (log.getUser() != null) {
            dto.setUserId(log.getUser().getId());
            dto.setUserName(log.getUser().getLogin());
            dto.setUserEmail(log.getUser().getEmail());
        }
        
        return dto;
    }

    /**
     * Obter IP real do cliente
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        // Se houver múltiplos IPs, pega o primeiro
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        // Normalizar IPv6 localhost para IPv4
        if (ip != null && (ip.equals("0:0:0:0:0:0:0:1") || ip.equals("::1"))) {
            ip = "127.0.0.1";
        }
        
        return ip;
    }
}
