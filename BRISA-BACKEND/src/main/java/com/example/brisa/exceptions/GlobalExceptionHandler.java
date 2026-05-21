package com.example.brisa.exceptions;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @org.springframework.beans.factory.annotation.Autowired
    private com.example.brisa.services.LogHelper logHelper;

    // Handler para a ValidationException
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(ValidationException ex, jakarta.servlet.http.HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Validation failed");
        response.put("details", ex.getErrors()); // Lista de erros detalhados

        try {
            boolean hasNivelamento = ex.getErrors() != null && ex.getErrors().stream().anyMatch(err -> err != null && err.toLowerCase().contains("nivelament"));
            if (hasNivelamento) {
                java.util.UUID userId = null;
                try {
                    var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
                    if (auth != null && auth.getPrincipal() instanceof com.example.brisa.models.auth.UserModel user) {
                        userId = user.getId();
                    }
                } catch (Exception ignored) {}
                String description = "Tentativa de vincular pessoa em dois nivelamentos simultaneos: " + String.join("; ", ex.getErrors());
                try {
                    logHelper.logAsync(com.example.brisa.enums.LogAction.SYSTEM_WARNING, description, "People", null, userId, request);
                } catch (Exception ignored) {}
            }
        } catch (Exception ignored) {}

        return ResponseEntity.badRequest().body(response); // Retorna os erros detalhados no corpo
    }

    // Handler para outras exceções (generais)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "An unexpected error occurred");
        response.put("message", ex.getMessage());
        return ResponseEntity.status(500).body(response); // Retorna um erro genérico com mensagem
    }
}
