package ru.practicum.collector.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.collector.service.UserActionService;
import ru.practicum.ewm.stats.proto.UserActionProto;

/**
 * @author Andrew Vilkov
 * @created 07.09.2026 - 12:18
 * @project java-plus-graduation
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class UserActionHandlerImpl implements UserActionHandler {
    private final UserActionService service;

    @Override
    public void handle(UserActionProto action) {
        log.info("User action received: {}", action);
        service.collectUserAction(action);
    }
}
