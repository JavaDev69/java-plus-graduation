package ru.practicum.collector.properties;

import jakarta.validation.constraints.NotBlank;
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
@ConfigurationProperties("collector.kafka.producer")
public class KafkaProperties {
    private Properties properties;
    @NotBlank
    private String topic;
}
