package ru.practicum.collector.controller;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.collector.handler.UserActionHandler;
import ru.practicum.ewm.stats.proto.UserActionControllerGrpc;
import ru.practicum.ewm.stats.proto.UserActionProto;

/**
 * @author Andrew Vilkov
 * @created 07.09.2026 - 11:01
 * @project java-plus-graduation
 */
@Slf4j
@RequiredArgsConstructor
@GrpcService
public class ActionGrpcController extends UserActionControllerGrpc.UserActionControllerImplBase {
    private final UserActionHandler handler;

    @Override
    public void collectUserAction(UserActionProto request, StreamObserver<Empty> responseObserver) {
        try {
            handler.handle(request);
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            responseObserver.onError(new StatusRuntimeException(Status.fromThrowable(ex)));
        }
    }
}
