package ru.practicum.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;


/**
 * DTO может использоваться для обновления и ответов
 */
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@Data
public class UserDto extends UserShortDto{

    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Email должен быть в корректном формате")
    private String email;

    public UserDto(Long id, String name, String email) {
        super(id, name);
        this.email = email;
    }
}
