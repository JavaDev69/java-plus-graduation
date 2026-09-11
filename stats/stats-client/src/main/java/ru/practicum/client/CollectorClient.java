package ru.practicum.client;

import com.google.protobuf.Timestamp;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.proto.ActionTypeProto;
import ru.practicum.ewm.stats.proto.UserActionControllerGrpc;
import ru.practicum.ewm.stats.proto.UserActionProto;

import java.time.Instant;

/**
 * @author Andrew Vilkov
 * @created 10.09.2026 - 20:24
 * @project java-plus-graduation
 */
@Slf4j
@Component
public class CollectorClient {
    @GrpcClient("collector")
    private UserActionControllerGrpc.UserActionControllerBlockingStub client;

    public void send(Long eventId, Long userId, ActionTypeProto actionType) {
        log.info("Sending action type: {}", actionType);
        Instant now = Instant.now();
        UserActionProto userAction = UserActionProto.newBuilder()
                .setEventId(eventId)
                .setUserId(userId)
                .setActionType(actionType)
                .setTimestamp(Timestamp.newBuilder()
                        .setNanos(now.getNano())
                        .setSeconds(now.getEpochSecond())
                        .build())
                .build();
        client.collectUserAction(userAction);
        log.info("Collected action type: {}", actionType);
    }
}
