package ru.practicum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author Andrew Vilkov
 * @created 18.08.2026 - 19:22
 * @project java-plus-graduation
 */
@EnableDiscoveryClient
@SpringBootApplication
public class StatsServiceApp {
    public static void main(String[] args) {
        SpringApplication.run(StatsServiceApp.class, args);
    }
}
