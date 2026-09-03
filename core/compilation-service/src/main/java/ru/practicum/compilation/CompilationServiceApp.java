package ru.practicum.compilation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import ru.practicum.StatsClient;
import ru.practicum.client.EventClient;
import ru.practicum.client.RateClient;
import ru.practicum.client.RequestClient;

/**
 * @author Andrew Vilkov
 * @created 27.08.2026 - 14:05
 * @project java-plus-graduation
 */
@EnableFeignClients(clients = {
        EventClient.class,
        RequestClient.class,
        RateClient.class,
        StatsClient.class
})
@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages = {"ru.practicum.compilation", "ru.practicum.error"})
public class CompilationServiceApp {
    public static void main(String[] args) {
        SpringApplication.run(CompilationServiceApp.class, args);
    }
}
