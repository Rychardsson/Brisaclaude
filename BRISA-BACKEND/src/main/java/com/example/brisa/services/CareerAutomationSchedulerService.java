package com.example.brisa.services;

import com.example.brisa.enums.LogAction;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CareerAutomationSchedulerService {

    private final CareerService careerService;
    private final SystemLogService systemLogService;

    @Scheduled(cron = "${career.automation.cron:0 0 */6 * * *}")
    public void runCareerAutomation() {
        try {
            careerService.processScheduledAutomation();
        } catch (Exception error) {
            Map<String, Object> details = new LinkedHashMap<>();
            details.put("operation", "SCHEDULE_ERROR");
            details.put("errorMessage", error.getMessage());

            systemLogService.createLog(
                    LogAction.SYSTEM_ERROR,
                    "Falha na execução agendada da automação de carreira",
                    "CareerAutomation",
                    null,
                    null,
                    null,
                    details
            );
        }
    }
}
