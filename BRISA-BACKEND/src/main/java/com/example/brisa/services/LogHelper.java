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
     */
    @Async
    public void logAsync(
            LogAction action,
            String description,
            String entityType,
            String entityId,
            UUID userId,
            HttpServletRequest request
    ) {
        systemLogService.createLog(action, description, entityType, entityId, userId, request, null);
    }

    /**
     * Log de criação de entidade
     */
    @Async
    public void logCreate(String entityType, String entityId, String entityName, UUID userId, HttpServletRequest request) {
        LogAction action = getCreateAction(entityType);
        String description = String.format("%s '%s' foi criado(a)", entityType, entityName);
        
        Map<String, Object> details = new HashMap<>();
        details.put("entityName", entityName);
        details.put("operation", "CREATE");
        
        systemLogService.createLog(action, description, entityType, entityId, userId, request, details);
    }

    /**
     * Log de atualização de entidade
     */
    @Async
    public void logUpdate(String entityType, String entityId, String entityName, UUID userId, HttpServletRequest request) {
        LogAction action = getUpdateAction(entityType);
        String description = String.format("%s '%s' foi atualizado(a)", entityType, entityName);
        
        Map<String, Object> details = new HashMap<>();
        details.put("entityName", entityName);
        details.put("operation", "UPDATE");
        
        systemLogService.createLog(action, description, entityType, entityId, userId, request, details);
    }

    /**
     * Log de exclusão de entidade
     */
    @Async
    public void logDelete(String entityType, String entityId, String entityName, UUID userId, HttpServletRequest request) {
        LogAction action = getDeleteAction(entityType);
        String description = String.format("%s '%s' foi excluído(a)", entityType, entityName);
        
        Map<String, Object> details = new HashMap<>();
        details.put("entityName", entityName);
        details.put("operation", "DELETE");
        
        systemLogService.createLog(action, description, entityType, entityId, userId, request, details);
    }

    /**
     * Log de login de usuário
     */
    @Async
    public void logLogin(UUID userId, String username, HttpServletRequest request) {
        String description = String.format("Usuário '%s' fez login no sistema", username);
        systemLogService.createLog(LogAction.USER_LOGIN, description, "User", userId.toString(), userId, request, null);
    }

    /**
     * Log de logout de usuário
     */
    @Async
    public void logLogout(UUID userId, String username, HttpServletRequest request) {
        String description = String.format("Usuário '%s' fez logout do sistema", username);
        systemLogService.createLog(LogAction.USER_LOGOUT, description, "User", userId.toString(), userId, request, null);
    }

    /**
     * Log de importação
     */
    @Async
    public void logImport(String entityType, int successCount, int errorCount, UUID userId, HttpServletRequest request) {
        LogAction action = getImportAction(entityType);
        String description = String.format("Importação de %s: %d sucesso, %d erros", entityType, successCount, errorCount);
        
        Map<String, Object> details = new HashMap<>();
        details.put("successCount", successCount);
        details.put("errorCount", errorCount);
        details.put("operation", "IMPORT");
        
        systemLogService.createLog(action, description, entityType, null, userId, request, details);
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
}
