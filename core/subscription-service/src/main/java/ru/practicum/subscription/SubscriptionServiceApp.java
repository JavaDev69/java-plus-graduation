package ru.practicum.subscription;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import ru.practicum.client.EventClient;
import ru.practicum.client.RequestClient;
import ru.practicum.client.UserClient;

/**
 * @author Andrew Vilkov
 * @created 27.08.2026 - 14:09
 * @project java-plus-graduation
 */
@EnableFeignClients(clients = {
        UserClient.class,
        EventClient.class,
        RequestClient.class
})
@EnableDiscoveryClient
@SpringBootApplication
public class SubscriptionServiceApp {
    public static void main(String[] args) {
        SpringApplication.run(SubscriptionServiceApp.class, args);
    }
}
