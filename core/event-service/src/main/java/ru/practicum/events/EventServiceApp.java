package ru.practicum.events;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import ru.practicum.StatsClient;
import ru.practicum.client.CategoryClient;
import ru.practicum.client.RateClient;
import ru.practicum.client.RequestClient;
import ru.practicum.client.UserClient;

/**
 * @author Andrew Vilkov
 * @created 27.08.2026 - 10:19
 */
@EnableFeignClients(clients = {
        StatsClient.class,
        CategoryClient.class,
        RequestClient.class,
        UserClient.class,
        RateClient.class
})
@EnableDiscoveryClient
@SpringBootApplication
public class EventServiceApp {
    public static void main(String[] args) {
        SpringApplication.run(EventServiceApp.class, args);
    }
}