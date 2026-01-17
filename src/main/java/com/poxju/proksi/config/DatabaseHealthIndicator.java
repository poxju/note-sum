package com.poxju.proksi.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * Custom database health indicator for Spring Boot Actuator.
 * Provides quick database connectivity checks for health monitoring.
 */
@Component
public class DatabaseHealthIndicator implements HealthIndicator {

    @Autowired
    private DataSource dataSource;

    @Override
    public Health health() {
        if (dataSource == null) {
            return Health.down()
                    .withDetail("error", "DataSource not configured")
                    .build();
        }

        try (Connection connection = dataSource.getConnection()) {
            boolean isValid = connection.isValid(2); // 2 second timeout for quick response
            if (isValid) {
                return Health.up()
                        .withDetail("database", "PostgreSQL")
                        .withDetail("validationQuery", "Connection valid")
                        .build();
            } else {
                return Health.down()
                        .withDetail("database", "PostgreSQL")
                        .withDetail("error", "Connection validation failed")
                        .build();
            }
        } catch (Exception e) {
            return Health.down()
                    .withDetail("database", "PostgreSQL")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
