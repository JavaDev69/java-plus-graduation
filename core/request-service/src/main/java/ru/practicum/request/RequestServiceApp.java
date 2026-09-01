package ru.practicum.request;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import ru.practicum.client.EventClient;
import ru.practicum.client.UserClient;

/**
 * @author Andrew Vilkov
 * @created 27.08.2026 - 14:08
 * @project java-plus-graduation
 */
@EnableFeignClients(clients = {
        EventClient.class,
        UserClient.class
})
@EnableDiscoveryClient
@SpringBootApplication
public class RequestServiceApp {
    public static void main(String[] args) {
        SpringApplication.run(RequestServiceApp.class, args);
    }
}
