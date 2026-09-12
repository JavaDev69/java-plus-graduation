package ru.practicum.analyzer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ConfigurableApplicationContext;
import ru.practicum.analyzer.processor.SimilarityProcessor;
import ru.practicum.analyzer.processor.UserActionsProcessor;

/**
 * @author Andrew Vilkov
 * @created 10.09.2026 - 13:03
 * @project java-plus-graduation
 */
@EnableDiscoveryClient
@ConfigurationPropertiesScan
@SpringBootApplication
public class AnalyzerApp {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(AnalyzerApp.class, args);
        SimilarityProcessor similarityProcessor = context.getBean(SimilarityProcessor.class);

        Thread similarityThread = new Thread(similarityProcessor);
        similarityThread.setName("SimilarityProcessorThread");
        similarityThread.start();

        UserActionsProcessor actionsProcessor = context.getBean(UserActionsProcessor.class);
        actionsProcessor.run();
    }
}
