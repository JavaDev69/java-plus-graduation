package ru.practicum.analyzer.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.Properties;

/**
 * @author Andrew Vilkov
 * @created 10.09.2026 - 14:56
 * @project java-plus-graduation
 */
public interface ConsumerProperties {
    Properties getProperties();
    String getTopic();
    Long getPollTimeoutMs();
}
