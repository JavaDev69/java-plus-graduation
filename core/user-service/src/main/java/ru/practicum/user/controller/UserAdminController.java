package ru.practicum.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.dto.user.NewUserRequest;
import ru.practicum.dto.user.UserDto;
import ru.practicum.operations.UserOperation;
import ru.practicum.user.service.UserService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@Validated
public class UserAdminController implements UserOperation {

    private final UserService userService;

    @Override
    public UserDto createUser(NewUserRequest request) {
        log.info("Начата обработка запроса на создание пользователя");
        log.debug("Получены данные для создания пользователя: name='{}', email='{}'",
                request.getName(), request.getEmail());

        UserDto dto = userService.save(request);

        log.info("Пользователь успешно создан с ID: {}", dto.getId());
        log.debug("Данные созданного пользователя: {}", dto);
        return dto;
    }

    @Override
    public List<UserDto> get(List<Long> ids, int offset, int size) {
        log.info("Начата обработка запроса на получение списка пользователей");
        log.debug("Параметры запроса: ids={}, offset={}, size={}",
                ids != null ? String.join(",", ids.toString()) : "null", offset, size);

        List<UserDto> users = userService.findByIdsOrAllWithPagination(ids, offset, size);

        log.info("Получено {} пользователей (от {}, размер страницы {})",
                users.size(), offset, size);
        log.debug("Список полученных пользователей: {}", users);
        return users;
    }

    @Override
    public void delete(Long userId) {
        log.info("Начата обработка запроса на удаление пользователя с ID: {}", userId);

        userService.deleteById(userId);

        log.info("Пользователь с ID {} успешно удалён", userId);
    }

    @Override
    public UserDto getById(Long id) {
        return userService.findById(id);
    }
}
