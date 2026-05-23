package com.example.brisa.services;

import com.example.brisa.dtos.log.LogStatsDTO;
import com.example.brisa.dtos.log.SystemLogDTO;
import com.example.brisa.dtos.log.SystemLogFilterDTO;
import com.example.brisa.enums.LogAction;
import com.example.brisa.models.SystemLogModel;
import com.example.brisa.repositories.SystemLogRepository;
import com.example.brisa.repositories.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SystemLogService {

    @Autowired
    private SystemLogRepository logRepository;

    @Autowired
    private UserRepository userRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

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

    @Transactional
    public SystemLogModel createLog(LogAction action, String description, UUID userId) {
        return createLog(action, description, null, null, userId, null, null);
    }

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

    public Page<SystemLogDTO> findLogs(SystemLogFilterDTO filter) {
        String sortBy = resolveSortBy(filter.getSortBy());
        Sort sort = Sort.by(
                filter.getSortDirection().equalsIgnoreCase("ASC")
                        ? Sort.Direction.ASC
                        : Sort.Direction.DESC,
                sortBy
        );

        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(), sort);
        String entityType = normalizeEntityType(filter.getEntityType());
        Specification<SystemLogModel> specification = buildSpecification(
                filter.getAction(),
                filter.getUserId(),
                entityType,
                filter.getStartDate(),
                filter.getEndDate()
        );

        Page<SystemLogModel> logs = logRepository.findAll(specification, pageable);

        return logs.map(this::convertToDTO);
    }

    public Optional<SystemLogDTO> findById(Long id) {
        return logRepository.findById(id).map(this::convertToDTO);
    }

    public List<SystemLogDTO> findRecentLogs(int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        return logRepository.findAll(pageable).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public LogStatsDTO getLogStats() {
        LogStatsDTO stats = new LogStatsDTO();

        stats.setTotalLogs(logRepository.count());

        List<Object[]> actionStats = logRepository.countByActionGrouped();
        Map<LogAction, Long> logsByAction = new HashMap<>();
        for (Object[] stat : actionStats) {
            logsByAction.put((LogAction) stat[0], (Long) stat[1]);
        }
        stats.setLogsByAction(logsByAction);

        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        Pageable pageable = PageRequest.of(0, 1);
        stats.setLogsToday(logRepository.findByCreatedAtBetween(startOfDay, endOfDay, pageable).getTotalElements());

        LocalDateTime startOfWeek = LocalDateTime.now().minusDays(7);
        stats.setLogsThisWeek(logRepository.findByCreatedAtBetween(startOfWeek, LocalDateTime.now(), pageable).getTotalElements());

        LocalDateTime startOfMonth = LocalDateTime.now().minusDays(30);
        stats.setLogsThisMonth(logRepository.findByCreatedAtBetween(startOfMonth, LocalDateTime.now(), pageable).getTotalElements());

        return stats;
    }

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

        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        if (ip != null && (ip.equals("0:0:0:0:0:0:0:1") || ip.equals("::1"))) {
            ip = "127.0.0.1";
        }

        return ip;
    }

    private String normalizeEntityType(String entityType) {
        if (entityType == null) {
            return null;
        }

        String normalized = entityType.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String resolveSortBy(String sortBy) {
        Set<String> allowedSortFields = Set.of("id", "action", "entityType", "entityId", "createdAt");
        return allowedSortFields.contains(sortBy) ? sortBy : "createdAt";
    }

    private Specification<SystemLogModel> buildSpecification(
            LogAction action,
            UUID userId,
            String entityType,
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new java.util.ArrayList<>();

            if (action != null) {
                predicates.add(criteriaBuilder.equal(root.get("action"), action));
            }

            if (userId != null) {
                predicates.add(criteriaBuilder.equal(root.get("user").get("id"), userId));
            }

            if (entityType != null) {
                predicates.add(criteriaBuilder.equal(root.get("entityType"), entityType));
            }

            if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), startDate));
            }

            if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), endDate));
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }
}
