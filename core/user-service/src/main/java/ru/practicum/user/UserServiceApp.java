package ru.practicum.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author Andrew Vilkov
 * @created 25.08.2026 - 13:37
 * @project java-plus-graduation
 */
@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages = {"ru.practicum.user","ru.practicum.error"})
public class UserServiceApp {
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApp.class, args);
    }
}
