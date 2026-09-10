package ru.practicum.analyzer.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.Properties;

/**
 * @author Andrew Vilkov
 * @created 07.09.2026 - 10:56
 * @project java-plus-graduation
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@ConfigurationProperties("analyzer.kafka.similarity-consumer")
@Validated
@ToString
public class SimilarityConsumerProperties implements ConsumerProperties{
    private Properties properties;
    @NotBlank
    private String topic;
    @NotNull @Positive
    private Long pollTimeoutMs;
}
