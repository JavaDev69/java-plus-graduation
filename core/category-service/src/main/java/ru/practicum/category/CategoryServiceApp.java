package ru.practicum.category;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import ru.practicum.client.EventClient;

/**
 * @author Andrew Vilkov
 * @created 27.08.2026 - 14:00
 * @project java-plus-graduation
 */
@EnableFeignClients(clients = EventClient.class)
@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages = {"ru.practicum.category", "ru.practicum.error"})
public class CategoryServiceApp {
    public static void main(String[] args) {
        SpringApplication.run(CategoryServiceApp.class, args);
    }
}
