package ru.practicum.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

/**
 * DTO может использоваться для ответов
 */
@Data
@SuperBuilder
@AllArgsConstructor
@EqualsAndHashCode
public class UserShortDto {

    @NotNull(message = "Идентификатор не может быть null")
    @Positive(message = "Идентификатор должен быть положительным числом (больше 0)")
    protected Long id;

    @NotBlank(message = "Имя не может быть пустым")
    protected String name;
}
