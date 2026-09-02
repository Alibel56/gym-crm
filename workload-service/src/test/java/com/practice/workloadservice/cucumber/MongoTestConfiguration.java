package com.practice.workloadservice.cucumber;

import com.practice.testtags.PositiveTest;
import com.practice.testtags.NegativeTest;
import com.practice.testtags.EdgeCaseTest;
import com.practice.testtags.ExceptionTest;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class MongoTestConfiguration {

    @Bean
    @ServiceConnection
    MongoDBContainer mongoDBContainer() {
        return new MongoDBContainer(
                DockerImageName.parse("mongo:7.0")
        );
    }
}
