package ru.practicum.category;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author Andrew Vilkov
 * @created 27.08.2026 - 14:00
 * @project java-plus-graduation
 */
@EnableDiscoveryClient
@SpringBootApplication
public class CategoryServiceApp {
    public static void main(String[] args) {
        SpringApplication.run(CategoryServiceApp.class, args);
    }
}
