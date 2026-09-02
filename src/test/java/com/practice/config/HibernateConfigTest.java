package com.practice.config;

import com.practice.testtags.EdgeCaseTest;
import com.practice.testtags.PositiveTest;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.test.util.ReflectionTestUtils;

import javax.sql.DataSource;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class HibernateConfigTest {

    private static final String DIALECT = "org.hibernate.dialect.PostgreSQLDialect";
    private static final boolean SHOW_SQL = true;
    private static final boolean FORMAT_SQL = true;
    private static final String DDL_AUTO = "validate";

    @Mock
    private DataSource dataSource;

    private HibernateConfig hibernateConfig;

    @BeforeEach
    void setUp() {
        hibernateConfig = new HibernateConfig();
        ReflectionTestUtils.setField(hibernateConfig, "dialect", DIALECT);
        ReflectionTestUtils.setField(hibernateConfig, "showSql", SHOW_SQL);
        ReflectionTestUtils.setField(hibernateConfig, "formatSql", FORMAT_SQL);
        ReflectionTestUtils.setField(hibernateConfig, "ddlAuto", DDL_AUTO);
    }

    @Test
    @PositiveTest
    void sessionFactory_setsGivenDataSource() {
        LocalSessionFactoryBean factory = hibernateConfig.sessionFactory(dataSource);

        DataSource configuredDataSource = (DataSource) ReflectionTestUtils.getField(factory, "dataSource");
        assertSame(dataSource, configuredDataSource);
    }

    @Test
    @PositiveTest
    void sessionFactory_setsEntityPackageToScan() {
        LocalSessionFactoryBean factory = hibernateConfig.sessionFactory(dataSource);

        String[] packagesToScan = (String[]) ReflectionTestUtils.getField(factory, "packagesToScan");
        assertNotNull(packagesToScan);
        assertArrayEquals(new String[]{"com.practice.entity"}, packagesToScan);
    }

    @Test
    @PositiveTest
    void sessionFactory_populatesHibernatePropertiesFromConfigValues() {
        LocalSessionFactoryBean factory = hibernateConfig.sessionFactory(dataSource);

        Properties props = (Properties) ReflectionTestUtils.getField(factory, "hibernateProperties");
        assertNotNull(props);
        assertEquals(DIALECT, props.get("hibernate.dialect"));
        assertEquals(SHOW_SQL, props.get("hibernate.show_sql"));
        assertEquals(FORMAT_SQL, props.get("hibernate.format_sql"));
        assertEquals(DDL_AUTO, props.get("hibernate.hbm2ddl.auto"));
    }

    @Test
    @PositiveTest
    void sessionFactory_setsSpringSessionContextClass() {
        LocalSessionFactoryBean factory = hibernateConfig.sessionFactory(dataSource);

        Properties props = (Properties) ReflectionTestUtils.getField(factory, "hibernateProperties");
        assertEquals(
                "org.springframework.orm.hibernate5.SpringSessionContext",
                props.get("hibernate.current_session_context_class")
        );
    }

    @Test
    @EdgeCaseTest
    void sessionFactory_withDifferentConfigValues_reflectsThemInProperties() {
        ReflectionTestUtils.setField(hibernateConfig, "dialect", "org.hibernate.dialect.H2Dialect");
        ReflectionTestUtils.setField(hibernateConfig, "showSql", false);
        ReflectionTestUtils.setField(hibernateConfig, "formatSql", false);
        ReflectionTestUtils.setField(hibernateConfig, "ddlAuto", "update");

        LocalSessionFactoryBean factory = hibernateConfig.sessionFactory(dataSource);

        Properties props = (Properties) ReflectionTestUtils.getField(factory, "hibernateProperties");
        assertEquals("org.hibernate.dialect.H2Dialect", props.get("hibernate.dialect"));
        assertEquals(false, props.get("hibernate.show_sql"));
        assertEquals(false, props.get("hibernate.format_sql"));
        assertEquals("update", props.get("hibernate.hbm2ddl.auto"));
    }

    @Test
    @PositiveTest
    void transactionManager_wrapsGivenSessionFactory() {
        SessionFactory sessionFactory = mock(SessionFactory.class);

        HibernateTransactionManager transactionManager = hibernateConfig.transactionManager(sessionFactory);

        assertNotNull(transactionManager);
        assertSame(sessionFactory, transactionManager.getSessionFactory());
    }
}
