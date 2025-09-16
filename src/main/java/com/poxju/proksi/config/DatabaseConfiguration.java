package com.poxju.proksi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.boot.jdbc.DataSourceBuilder;

/**
 * Converts Railway provided DATABASE_URL (postgresql://user:pw@host:port/db) into
 * a JDBC URL (jdbc:postgresql://host:port/db) plus extracted credentials.
 */
@Configuration
public class DatabaseConfiguration {

    @Bean
    @Primary
    @ConditionalOnProperty(name = "DATABASE_URL")
    public DataSource railwayDataSource(@Value("${DATABASE_URL}") String databaseUrl) throws URISyntaxException {
        // Accept both postgresql:// and jdbc:postgresql:// to be idempotent
        String raw = databaseUrl.trim();
        if (raw.startsWith("jdbc:postgresql://")) {
            // Already jdbc form; let Spring Boot handle via normal properties instead of duplicating
            return DataSourceBuilder.create().url(raw).build();
        }
        if (!raw.startsWith("postgresql://")) {
            throw new IllegalArgumentException("Unsupported DATABASE_URL scheme: " + raw);
        }
        URI uri = new URI(raw);
        String[] userInfo = uri.getUserInfo().split(":", 2);
        String username = userInfo[0];
        String password = userInfo.length > 1 ? userInfo[1] : "";
        String jdbcUrl = "jdbc:postgresql://" + uri.getHost() + (uri.getPort() > 0 ? ":" + uri.getPort() : "") + uri.getPath();
        // Propagate query params (sslmode, etc.) if present
        if (uri.getQuery() != null && !uri.getQuery().isBlank()) {
            jdbcUrl += "?" + uri.getQuery();
        }
        return DataSourceBuilder.create()
                .url(jdbcUrl)
                .username(username)
                .password(password)
                .driverClassName("org.postgresql.Driver")
                .build();
    }
}
