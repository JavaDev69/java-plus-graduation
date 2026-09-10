package ru.practicum.analyzer.controller;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.analyzer.service.RecommendationService;
import ru.practicum.ewm.stats.proto.InteractionsCountRequestProto;
import ru.practicum.ewm.stats.proto.RecommendationsControllerGrpc;
import ru.practicum.ewm.stats.proto.RecommendedEventProto;
import ru.practicum.ewm.stats.proto.SimilarEventsRequestProto;
import ru.practicum.ewm.stats.proto.UserPredictionsRequestProto;

/**
 * @author Andrew Vilkov
 * @created 10.09.2026 - 14:34
 * @project java-plus-graduation
 */
@Slf4j
@RequiredArgsConstructor
@GrpcService
public class AnalyzerGrpcController extends RecommendationsControllerGrpc.RecommendationsControllerImplBase {
    private final RecommendationService recommendationService;

    @Override
    public void getRecommendationsForUser(UserPredictionsRequestProto request, StreamObserver<RecommendedEventProto> responseObserver) {
        log.info("getRecommendationsForUser: {}", request);
        Runnable runnable = () -> recommendationService
                .getRecommendationsForUser(request)
                .forEach(responseObserver::onNext);
        handle(runnable, responseObserver);
    }

    @Override
    public void getSimilarEvents(SimilarEventsRequestProto request, StreamObserver<RecommendedEventProto> responseObserver) {
        log.info("getSimilarEvents: {}", request);

        Runnable runnable = () -> recommendationService
                .getSimilarEvents(request)
                .forEach(responseObserver::onNext);
        handle(runnable, responseObserver);
    }

    @Override
    public void getInteractionsCount(InteractionsCountRequestProto request, StreamObserver<RecommendedEventProto> responseObserver) {
        log.info("getInteractionsCount: {}", request);

        Runnable runnable = () -> recommendationService
                .getInteractionsCount(request)
                .forEach(responseObserver::onNext);
        handle(runnable, responseObserver);
    }

    private void handle(Runnable runnable, StreamObserver<RecommendedEventProto> responseObserver) {
        try {
            runnable.run();
            responseObserver.onCompleted();
        } catch (Exception ex) {
            responseObserver.onError(new StatusRuntimeException(Status.fromThrowable(ex)));
        }
    }
}
