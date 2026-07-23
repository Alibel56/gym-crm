package com.practice.health;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;

class DatabaseLatencyHealthIndicatorTest {

    private DatabaseLatencyHealthIndicator healthIndicator;

    @BeforeEach
    void setUp() {
        healthIndicator = new DatabaseLatencyHealthIndicator();
    }

    @Test
    void health_whenDatabaseReachable_reportsUpWithResponseTime() {
        healthIndicator.setDataSource(inMemoryH2DataSource());

        Health health = healthIndicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsKey("responseTimeMs");
    }

    @Test
    void health_whenDatabaseUnreachable_reportsDownWithError() {
        DataSource brokenDataSource = brokenDataSource();
        healthIndicator.setDataSource(brokenDataSource);

        Health health = healthIndicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsKey("error");
    }

    private DataSource inMemoryH2DataSource() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:health-check;DB_CLOSE_DELAY=-1");
        dataSource.setUser("sa");
        dataSource.setPassword("");
        return dataSource;
    }

    private DataSource brokenDataSource() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:does-not-exist;IFEXISTS=TRUE");
        dataSource.setUser("sa");
        dataSource.setPassword("");
        return dataSource;
    }
}
