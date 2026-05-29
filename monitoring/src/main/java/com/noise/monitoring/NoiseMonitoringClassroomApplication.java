package com.noise.monitoring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.integration.config.EnableIntegration;

@SpringBootApplication

@EnableIntegration
public class NoiseMonitoringClassroomApplication {

    public static void main(String[] args) {

        SpringApplication.run(
                NoiseMonitoringClassroomApplication.class,
                args
        );
    }
}