package com.example.brisa.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.example.brisa.enums.LogAction;
import com.example.brisa.models.SystemLogModel;

import jakarta.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Helper service para facilitar o registro de logs em todo o sistema
 */
@Service
public class LogHelper {

    @Autowired
    private SystemLogService systemLogService;

    /**
     * Registra um log de forma assíncrona (não bloqueia a execução)
     * Extrai dados do request ANTES de chamar o método async
     */
    public void logAsync(
            LogAction action,
            String description,
            String entityType,
            String entityId,
            UUID userId,
            HttpServletRequest request
    ) {
        String ipAddress = extractClientIp(request);
        String userAgent = extractUserAgent(request);
        logAsyncInternal(action, description, entityType, entityId, userId, ipAddress, userAgent);
    }

    /**
     * Versão interna assíncrona de logAsync
     */
    @Async
    private void logAsyncInternal(
            LogAction action,
            String description,
            String entityType,
            String entityId,
            UUID userId,
            String ipAddress,
            String userAgent
    ) {
        systemLogService.createLogWithRequestData(action, description, entityType, entityId, userId, ipAddress, userAgent, null);
    }

    /**
     * Log de criação de entidade
     * Extrai dados do request ANTES de chamar o método async
     */
    public void logCreate(String entityType, String entityId, String entityName, UUID userId, HttpServletRequest request) {
        String ipAddress = extractClientIp(request);
        String userAgent = extractUserAgent(request);
        
        Map<String, Object> details = new HashMap<>();
        details.put("entityName", entityName);
        details.put("operation", "CREATE");
        
        logCreateInternal(entityType, entityId, entityName, userId, ipAddress, userAgent, details);
    }

    /**
     * Versão interna assíncrona de logCreate
     */
    @Async
    private void logCreateInternal(String entityType, String entityId, String entityName, UUID userId, String ipAddress, String userAgent, Map<String, Object> details) {
        LogAction action = getCreateAction(entityType);
        String description = String.format("%s '%s' foi criado(a)", entityType, entityName);
        systemLogService.createLogWithRequestData(action, description, entityType, entityId, userId, ipAddress, userAgent, details);
    }

    /**
     * Log de atualização de entidade
     * Extrai dados do request ANTES de chamar o método async
     */
    public void logUpdate(String entityType, String entityId, String entityName, UUID userId, HttpServletRequest request) {
        String ipAddress = extractClientIp(request);
        String userAgent = extractUserAgent(request);
        
        Map<String, Object> details = new HashMap<>();
        details.put("entityName", entityName);
        details.put("operation", "UPDATE");
        
        logUpdateInternal(entityType, entityId, entityName, userId, ipAddress, userAgent, details);
    }

    /**
     * Versão interna assíncrona de logUpdate
     */
    @Async
    private void logUpdateInternal(String entityType, String entityId, String entityName, UUID userId, String ipAddress, String userAgent, Map<String, Object> details) {
        LogAction action = getUpdateAction(entityType);
        String description = String.format("%s '%s' foi atualizado(a)", entityType, entityName);
        systemLogService.createLogWithRequestData(action, description, entityType, entityId, userId, ipAddress, userAgent, details);
    }

    /**
     * Log de exclusão de entidade
     * Extrai dados do request ANTES de chamar o método async
     */
    public void logDelete(String entityType, String entityId, String entityName, UUID userId, HttpServletRequest request) {
        String ipAddress = extractClientIp(request);
        String userAgent = extractUserAgent(request);
        
        Map<String, Object> details = new HashMap<>();
        details.put("entityName", entityName);
        details.put("operation", "DELETE");
        
        logDeleteInternal(entityType, entityId, entityName, userId, ipAddress, userAgent, details);
    }

    /**
     * Versão interna assíncrona de logDelete
     */
    @Async
    private void logDeleteInternal(String entityType, String entityId, String entityName, UUID userId, String ipAddress, String userAgent, Map<String, Object> details) {
        LogAction action = getDeleteAction(entityType);
        String description = String.format("%s '%s' foi excluído(a)", entityType, entityName);
        systemLogService.createLogWithRequestData(action, description, entityType, entityId, userId, ipAddress, userAgent, details);
    }

    /**
     * Log de login de usuário (com extração segura de dados do request)
     * Extrai IP e User-Agent ANTES de chamar o método async
     */
    public void logLogin(UUID userId, String username, HttpServletRequest request) {
        String ipAddress = extractClientIp(request);
        String userAgent = extractUserAgent(request);
        logLoginAsync(userId, username, ipAddress, userAgent);
    }

    /**
     * Log de login de usuário (versão interna assíncrona)
     */
    @Async
    private void logLoginAsync(UUID userId, String username, String ipAddress, String userAgent) {
        String description = String.format("Usuário '%s' fez login no sistema", username);
        systemLogService.createLogWithRequestData(LogAction.USER_LOGIN, description, "User", userId.toString(), userId, ipAddress, userAgent, null);
    }

    /**
     * Log de logout de usuário (com extração segura de dados do request)
     * Extrai IP e User-Agent ANTES de chamar o método async
     */
    public void logLogout(UUID userId, String username, HttpServletRequest request) {
        String ipAddress = extractClientIp(request);
        String userAgent = extractUserAgent(request);
        logLogoutAsync(userId, username, ipAddress, userAgent);
    }

    /**
     * Log de logout de usuário (versão interna assíncrona)
     */
    @Async
    private void logLogoutAsync(UUID userId, String username, String ipAddress, String userAgent) {
        String description = String.format("Usuário '%s' fez logout do sistema", username);
        systemLogService.createLogWithRequestData(LogAction.USER_LOGOUT, description, "User", userId.toString(), userId, ipAddress, userAgent, null);
    }

    /**
     * Log de importação
     * Extrai dados do request ANTES de chamar o método async
     */
    public void logImport(String entityType, int successCount, int errorCount, UUID userId, HttpServletRequest request) {
        String ipAddress = extractClientIp(request);
        String userAgent = extractUserAgent(request);
        
        Map<String, Object> details = new HashMap<>();
        details.put("successCount", successCount);
        details.put("errorCount", errorCount);
        details.put("operation", "IMPORT");
        
        logImportInternal(entityType, successCount, errorCount, userId, ipAddress, userAgent, details);
    }

    /**
     * Versão interna assíncrona de logImport
     */
    @Async
    private void logImportInternal(String entityType, int successCount, int errorCount, UUID userId, String ipAddress, String userAgent, Map<String, Object> details) {
        LogAction action = getImportAction(entityType);
        String description = String.format("Importação de %s: %d sucesso, %d erros", entityType, successCount, errorCount);
        systemLogService.createLogWithRequestData(action, description, entityType, null, userId, ipAddress, userAgent, details);
    }

    /**
     * Log de erro no sistema
     */
    @Async
    public void logError(String description, String entityType, Exception exception, UUID userId) {
        Map<String, Object> details = new HashMap<>();
        details.put("errorMessage", exception.getMessage());
        details.put("errorClass", exception.getClass().getName());
        
        systemLogService.createLog(LogAction.SYSTEM_ERROR, description, entityType, null, userId, null, details);
    }

    // Métodos auxiliares para mapear tipos de entidade para ações
    
    private LogAction getCreateAction(String entityType) {
        return switch (entityType.toLowerCase()) {
            case "people" -> LogAction.PEOPLE_CREATE;
            case "program" -> LogAction.PROGRAM_CREATE;
            case "class" -> LogAction.CLASS_CREATE;
            case "stage" -> LogAction.STAGE_CREATE;
            case "enrollment" -> LogAction.ENROLLMENT_CREATE;
            case "institution" -> LogAction.INSTITUTION_CREATE;
            case "user" -> LogAction.USER_REGISTER;
            default -> LogAction.SYSTEM_INFO;
        };
    }

    private LogAction getUpdateAction(String entityType) {
        return switch (entityType.toLowerCase()) {
            case "people" -> LogAction.PEOPLE_UPDATE;
            case "program" -> LogAction.PROGRAM_UPDATE;
            case "class" -> LogAction.CLASS_UPDATE;
            case "stage" -> LogAction.STAGE_UPDATE;
            case "enrollment" -> LogAction.ENROLLMENT_UPDATE;
            case "institution" -> LogAction.INSTITUTION_UPDATE;
            case "user" -> LogAction.USER_UPDATE;
            default -> LogAction.SYSTEM_INFO;
        };
    }

    private LogAction getDeleteAction(String entityType) {
        return switch (entityType.toLowerCase()) {
            case "people" -> LogAction.PEOPLE_DELETE;
            case "program" -> LogAction.PROGRAM_DELETE;
            case "class" -> LogAction.CLASS_DELETE;
            case "stage" -> LogAction.STAGE_DELETE;
            case "enrollment" -> LogAction.ENROLLMENT_DELETE;
            case "institution" -> LogAction.INSTITUTION_DELETE;
            case "user" -> LogAction.USER_DELETE;
            default -> LogAction.SYSTEM_INFO;
        };
    }

    private LogAction getImportAction(String entityType) {
        return switch (entityType.toLowerCase()) {
            case "people" -> LogAction.PEOPLE_IMPORT;
            case "program" -> LogAction.PROGRAM_IMPORT;
            case "class" -> LogAction.CLASS_IMPORT;
            case "enrollment" -> LogAction.ENROLLMENT_IMPORT;
            case "institution" -> LogAction.INSTITUTION_IMPORT;
            case "stagecandidate", "stage_candidate" -> LogAction.STAGE_CANDIDATES_IMPORT;
            default -> LogAction.SYSTEM_INFO;
        };
    }

    /**
     * Extrai o IP do cliente de forma segura
     */
    private String extractClientIp(HttpServletRequest request) {
        if (request == null) return "UNKNOWN";
        
        try {
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
            
            return ip != null ? ip : "UNKNOWN";
        } catch (Exception e) {
            return "UNKNOWN";
        }
    }

    /**
     * Extrai o User-Agent de forma segura
     */
    private String extractUserAgent(HttpServletRequest request) {
        if (request == null) return "UNKNOWN";
        
        try {
            String userAgent = request.getHeader("User-Agent");
            return userAgent != null && !userAgent.isEmpty() ? userAgent : "UNKNOWN";
        } catch (Exception e) {
            return "UNKNOWN";
        }
    }
}
