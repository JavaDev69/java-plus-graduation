package ru.practicum.operations;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.practicum.dto.events.EventState;
import ru.practicum.dto.user.NewUserRequest;
import ru.practicum.dto.user.UserDto;
import ru.practicum.exception.NotFoundException;

import java.util.List;
import java.util.Map;

/**
 * @author Andrew Vilkov
 * @created 25.08.2026 - 15:29
 * @project java-plus-graduation
 */
public interface RequestOperation {

    @GetMapping
    Map<Long,Long> countRequestsByEventIdsAndStatus(
            @RequestBody @NotEmpty List<Long> eventIds,
            @RequestParam EventState state);
}
