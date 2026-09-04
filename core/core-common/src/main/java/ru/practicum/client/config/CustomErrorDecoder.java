package ru.practicum.client.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import ru.practicum.error.ErrorResponse;
import ru.practicum.exception.NotFoundException;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Andrew Vilkov
 * @created 19.08.2026 - 16:17
 * @project java-plus-graduation
 */
@Slf4j
public class CustomErrorDecoder implements ErrorDecoder {
    private final ErrorDecoder defaultErrorDecoder = new Default();
    private final ObjectMapper mapper = new ObjectMapper();

    public CustomErrorDecoder() {
        mapper.registerModule(new JavaTimeModule());
    }

    @Override
    public Exception decode(String methodKey, Response response) {
        log.debug("Decoding error response for methodKey: {}, response: {}", methodKey, response);

        if (response.status() == 404) {
            String clientName = methodKey.split("#")[0];
            String subject = switch (clientName) {
                case "UserClient" -> "Пользователь";
                case "RequestClient" -> "Запрос";
                case "RateClient" -> "Рейтинг";
                case "EventClient" -> "Событие";
                case "CategoryClient" -> "Категория";
                case "AdminCommentClient" -> "Комментарий";
                default -> "Элемент";
            };

            String message = getMessageFromBody(response.body());

            return new NotFoundException("%s не найден. Message: %s".formatted(subject, message));
        }
        return defaultErrorDecoder.decode(methodKey, response);
    }

    private String getMessageFromBody(Response.Body body) {
        if (body == null) {
            return "none";
        }

        try (InputStream is = body.asInputStream()) {
            ErrorResponse errorResponse = mapper.readValue(is, ErrorResponse.class);
            return errorResponse.getMessage();
        } catch (IOException ioe) {
            log.error("Error occurred while trying to read response body", ioe);
        }
        return "unknown message";
    }
}
