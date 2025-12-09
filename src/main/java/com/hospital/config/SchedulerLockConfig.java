package com.hospital.config;

import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * Configuration for ShedLock distributed scheduler locking.
 *
 * <p>Ensures that scheduled tasks with @SchedulerLock only execute once
 * across multiple application instances, preventing concurrent execution
 * and race conditions.</p>
 *
 * <p><strong>Database Schema:</strong></p>
 * <pre>
 * CREATE TABLE shedlock (
 *     name VARCHAR(64) PRIMARY KEY,
 *     lock_until TIMESTAMP NOT NULL,
 *     locked_at TIMESTAMP NOT NULL,
 *     locked_by VARCHAR(255) NOT NULL
 * );
 * </pre>
 *
 * @author Hospital Revenue Cycle System
 * @version 1.0.0
 */
@Configuration
@EnableSchedulerLock(defaultLockAtMostFor = "30m")
public class SchedulerLockConfig {

    /**
     * Creates a JDBC-based lock provider for ShedLock.
     *
     * <p>Uses the application's primary datasource to store distributed locks
     * in the 'shedlock' table.</p>
     *
     * @param dataSource the application's primary datasource
     * @return configured lock provider
     */
    @Bean
    public LockProvider lockProvider(DataSource dataSource) {
        return new JdbcTemplateLockProvider(JdbcTemplateLockProvider.Configuration.builder()
            .withJdbcTemplate(new JdbcTemplate(dataSource))
            .usingDbTime() // Use database time for consistency across instances
            .build()
        );
    }
}
