package com.example.brisa.infra.database;

import com.example.brisa.enums.LogAction;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SystemLogConstraintSynchronizer implements ApplicationRunner {

    private static final String TABLE_NAME = "system_logs";
    private static final String CONSTRAINT_NAME = "system_logs_action_check";

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        try {
            if (!systemLogsTableExists()) {
                return;
            }

            String allowedActions = Arrays.stream(LogAction.values())
                    .map(LogAction::name)
                    .map(action -> "'" + action + "'")
                    .collect(Collectors.joining(", "));

            jdbcTemplate.execute("ALTER TABLE " + TABLE_NAME + " DROP CONSTRAINT IF EXISTS " + CONSTRAINT_NAME);
            jdbcTemplate.execute(
                    "ALTER TABLE " + TABLE_NAME +
                            " ADD CONSTRAINT " + CONSTRAINT_NAME +
                            " CHECK (action IN (" + allowedActions + "))"
            );

            log.info("System log action constraint synchronized with {} enum values.", LogAction.values().length);
        } catch (Exception exception) {
            log.warn("Could not synchronize system log action constraint automatically: {}", exception.getMessage());
        }
    }

    private boolean systemLogsTableExists() {
        return Boolean.TRUE.equals(jdbcTemplate.execute((ConnectionCallback<Boolean>) connection -> {
            try (ResultSet tables = connection.getMetaData().getTables(null, null, TABLE_NAME, null)) {
                return tables.next();
            }
        }));
    }
}
