package ru.practicum.collector.handler;

import ru.practicum.ewm.stats.proto.UserActionProto;

/**
 * @author Andrew Vilkov
 * @created 07.09.2026 - 12:17
 * @project java-plus-graduation
 */
public interface UserActionHandler {

    void handle(UserActionProto action);
}
