package ru.practicum.analyzer.properties;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * @author Andrew Vilkov
 * @created 09.09.2026 - 14:40
 * @project java-plus-graduation
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@ConfigurationProperties("analyzer.event-weight")
public class WeightProperties {
    @NotNull @DecimalMin("0.") @DecimalMax("1.")
    private Double view;

    @NotNull @DecimalMin("0.") @DecimalMax("1.")
    private Double register;

    @NotNull @DecimalMin("0.") @DecimalMax("1.")
    private Double like;
}
