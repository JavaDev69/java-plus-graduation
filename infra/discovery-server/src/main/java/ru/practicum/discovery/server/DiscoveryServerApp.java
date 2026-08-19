package ru.practicum.discovery.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * @author Andrew Vilkov
 * @created 19.08.2026 - 14:43
 * @project java-plus-graduation
 */
@EnableEurekaServer
@SpringBootApplication
public class DiscoveryServerApp {
    public static void main(String[] args) {
        SpringApplication.run(DiscoveryServerApp.class, args);
    }
}
