package ru.practicum.collector;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author Andrew Vilkov
 * @created 07.09.2026 - 10:54
 * @project java-plus-graduation
 */
@ConfigurationPropertiesScan
@EnableDiscoveryClient
@SpringBootApplication
public class CollectorApp {
    public static void main(String[] args) {
        SpringApplication.run(CollectorApp.class, args);
    }
}
