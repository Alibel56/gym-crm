package com.practice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
public class GymCrmApplication {

    private static final Logger log = LoggerFactory.getLogger(GymCrmApplication.class);

    public static void main(String[] args) {
        log.info("Starting Gym CRM application");
        SpringApplication.run(GymCrmApplication.class, args);
        log.info("Gym CRM application started successfully");
    }
}
