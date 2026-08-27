package ru.practicum.operations;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.practicum.dto.user.NewUserRequest;
import ru.practicum.dto.user.UserDto;
import ru.practicum.exception.NotFoundException;

import java.util.List;

/**
 * @author Andrew Vilkov
 * @created 25.08.2026 - 15:29
 * @project java-plus-graduation
 */
public interface UserOperation {

    /**
     * Создаёт нового пользователя через административный API.
     *
     * @param request DTO с данными нового пользователя (имя и email).
     *                Обязательные поля:
     *                - name: не должно быть пустым, длина от 1 до 255 символов
     *                - email: должен быть корректным email-адресом
     * @return ResponseEntity с UserDto и HTTP‑статусом 201 (CREATED)
     */
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    UserDto createUser(@Valid @RequestBody NewUserRequest request);

    /**
     * Получает список пользователей по заданным критериям.
     *
     * @param ids    необязательный список ID пользователей для фильтрации.
     *               Если не указан — возвращаются все пользователи.
     * @param offset индекс начала выборки (нумерация с 0).
     *               Минимальное значение: 0.
     * @param size   размер страницы результатов.
     *               Минимальное значение: 1.
     * @return список UserDto, соответствующий критериям поиска
     * @throws ConstraintViolationException если параметры не прошли валидацию
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    List<UserDto> get(
            @RequestParam(name = "ids", required = false) List<Long> ids,
            @RequestParam(defaultValue = "0") @Min(0) int offset,
            @RequestParam(defaultValue = "10") @Min(1) int size);

    /**
     * Удаляет пользователя по его ID.
     *
     * @param userId уникальный идентификатор пользователя.
     *               Должен быть положительным числом (> 0)
     * @throws NotFoundException            если пользователь с указанным ID не найден
     * @throws ConstraintViolationException если ID не прошёл валидацию
     */
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("{userId}")
    void delete(@PathVariable @Positive Long userId);

    /**
     * Получает пользователя по {@code id}
     * @param id id пользователя
     * @return пользователь с {@code id}
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{userId}")
    UserDto getById(@PathVariable(name = "userId") Long id);
}
