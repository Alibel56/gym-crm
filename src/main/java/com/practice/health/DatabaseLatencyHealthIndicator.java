package com.practice.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;


@Component("databaseLatency")
public class DatabaseLatencyHealthIndicator implements HealthIndicator {

    private static final Logger log = LoggerFactory.getLogger(DatabaseLatencyHealthIndicator.class);
    private static final long DEGRADED_THRESHOLD_MS = 500L;

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public Health health() {
        long start = System.currentTimeMillis();

        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
        } catch (Exception ex) {
            log.error("Health check: database latency probe failed", ex);
            return Health.down(ex).build();
        }

        long elapsedMs = System.currentTimeMillis() - start;

        Health.Builder status = elapsedMs > DEGRADED_THRESHOLD_MS ? Health.status("DEGRADED") : Health.up();

        if (elapsedMs > DEGRADED_THRESHOLD_MS) {
            log.warn("Health check: database latency degraded, responseTimeMs={}", elapsedMs);
        }

        return status.withDetail("responseTimeMs", elapsedMs).build();
    }
}
