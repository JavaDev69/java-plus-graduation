package ru.practicum.client.config;

import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Andrew Vilkov
 * @created 19.08.2026 - 16:17
 * @project java-plus-graduation
 */
@Slf4j
public class CustomErrorDecoder implements ErrorDecoder {
    private final ErrorDecoder defaultErrorDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        log.debug("Decoding error response for methodKey: {}, response: {}", methodKey, response);
        return defaultErrorDecoder.decode(methodKey, response);
    }
}
