package com.practice.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.util.ReflectionTestUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverPropertyInfo;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

class DataSourceConfigTest {

    private static final String URL = "jdbc:fakedb://localhost:5432/gym";
    private static final String USERNAME = "gym_user";
    private static final String PASSWORD = "gym_password";

    private DataSourceConfig dataSourceConfig;

    @BeforeEach
    void setUp() {
        dataSourceConfig = new DataSourceConfig();
        ReflectionTestUtils.setField(dataSourceConfig, "driver", FakeDriver.class.getName());
        ReflectionTestUtils.setField(dataSourceConfig, "url", URL);
        ReflectionTestUtils.setField(dataSourceConfig, "username", USERNAME);
        ReflectionTestUtils.setField(dataSourceConfig, "password", PASSWORD);
    }

    @Test
    void dataSource_returnsDriverManagerDataSourceInstance() {
        DataSource dataSource = dataSourceConfig.dataSource();

        assertInstanceOf(DriverManagerDataSource.class, dataSource);
    }

    @Test
    void dataSource_validDriverClassName_doesNotThrow() {
        // setDriverClassName() only does Class.forName(...) to trigger the driver's static
        // registration with java.sql.DriverManager - it stores no field we could inspect.
        // The meaningful thing to verify is that a loadable class name doesn't blow up.
        assertDoesNotThrow(() -> dataSourceConfig.dataSource());
    }

    @Test
    void dataSource_setsUrlFromConfig() {
        DriverManagerDataSource dataSource = (DriverManagerDataSource) dataSourceConfig.dataSource();

        assertEquals(URL, dataSource.getUrl());
    }

    @Test
    void dataSource_setsUsernameFromConfig() {
        DriverManagerDataSource dataSource = (DriverManagerDataSource) dataSourceConfig.dataSource();

        assertEquals(USERNAME, dataSource.getUsername());
    }

    @Test
    void dataSource_setsPasswordFromConfig() {
        DriverManagerDataSource dataSource = (DriverManagerDataSource) dataSourceConfig.dataSource();

        assertEquals(PASSWORD, dataSource.getPassword());
    }

    @Test
    void dataSource_withDifferentUrlAndCredentials_reflectsThemInBean() {
        ReflectionTestUtils.setField(dataSourceConfig, "url", "jdbc:fakedb:mem:testdb");
        ReflectionTestUtils.setField(dataSourceConfig, "username", "sa");
        ReflectionTestUtils.setField(dataSourceConfig, "password", "");

        DriverManagerDataSource dataSource = (DriverManagerDataSource) dataSourceConfig.dataSource();

        assertEquals("jdbc:fakedb:mem:testdb", dataSource.getUrl());
        assertEquals("sa", dataSource.getUsername());
        assertEquals("", dataSource.getPassword());
    }

    @Test
    void dataSource_unknownDriverClassName_throwsIllegalStateException() {
        ReflectionTestUtils.setField(dataSourceConfig, "driver", "com.practice.NonExistentDriver");

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> dataSourceConfig.dataSource());

        assertTrue(ex.getMessage().contains("com.practice.NonExistentDriver"));
    }

    @Test
    void dataSource_calledTwice_returnsNewInstanceEachTime() {
        DataSource first = dataSourceConfig.dataSource();
        DataSource second = dataSourceConfig.dataSource();

        assertNotSame(first, second);
    }

    public static class FakeDriver implements Driver {
        @Override
        public Connection connect(String url, Properties info) {
            return null;
        }

        @Override
        public boolean acceptsURL(String url) {
            return true;
        }

        @Override
        public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) {
            return new DriverPropertyInfo[0];
        }

        @Override
        public int getMajorVersion() {
            return 1;
        }

        @Override
        public int getMinorVersion() {
            return 0;
        }

        @Override
        public boolean jdbcCompliant() {
            return false;
        }

        @Override
        public Logger getParentLogger() throws SQLFeatureNotSupportedException {
            throw new SQLFeatureNotSupportedException();
        }
    }
}