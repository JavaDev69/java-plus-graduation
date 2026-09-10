package ru.practicum.aggregator.properties;

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
@ConfigurationProperties("aggregator.event-weight")
@Validated
@ToString
public class WeightProperties {
    @NotNull @DecimalMin("0.") @DecimalMax("1.")
    Double view;

    @NotNull @DecimalMin("0.") @DecimalMax("1.")
    Double register;

    @NotNull @DecimalMin("0.") @DecimalMax("1.")
    Double like;
}
