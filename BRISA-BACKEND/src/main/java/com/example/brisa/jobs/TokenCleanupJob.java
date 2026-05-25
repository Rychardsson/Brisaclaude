package com.example.brisa.jobs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.brisa.repositories.PasswordResetTokenRepository;

import java.time.LocalDateTime;

@Component
public class TokenCleanupJob {

    @Autowired
    private PasswordResetTokenRepository resetTokenRepository;

    @Scheduled(cron = "0 0 */6 * * *") // Executar a cada 6 horas
    public void cleanupExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        resetTokenRepository.deleteByExpiryDateBefore(now);
        System.out.println("Tokens de redefinição de senha expirados foram removidos em: " + now);
    }
}
