package com.daham.grpc;

import com.daham.core.domain.Measurement;
import com.daham.core.services.MeasurementService;
import com.daham.core.utils.ObjectMapperUtils;
import io.grpc.stub.StreamObserver;
import io.quarkus.grpc.GrpcService;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
@Transactional
@GrpcService
public class MeasurementGrpcServer extends MeasurementServiceGrpc.MeasurementServiceImplBase {
  private final Queue<Measurement> queue;

  @Inject
  MeasurementService measurementService;

  public MeasurementGrpcServer() {
    queue = new ConcurrentLinkedQueue<>();
  }

  @Override
  public StreamObserver<MeasurementProto.Measurement> streamMeasurements(StreamObserver<MeasurementProto.Reply> responseObserver) {
    return new StreamObserver<>() {
      @Override
      public void onNext(MeasurementProto.Measurement inputMeasurement) {
        var measurement = ObjectMapperUtils.map(inputMeasurement, Measurement.class);
        var inputTimestamp = inputMeasurement.getTimestamp();
        measurement.setTimestamp(Instant
            .ofEpochSecond(inputTimestamp.getSeconds(), inputTimestamp.getNanos())
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime());
        queue.add(measurement);
      }

      @Override
      public void onError(Throwable throwable) {
        log.error("StreamObserver ERROR: {}", throwable.toString());
      }

      @Override
      public void onCompleted() {
        var replyBuilder = MeasurementProto.Reply.newBuilder();
        while (!queue.isEmpty()) {
          try {
            var measurement = queue.remove();
            measurement = measurementService.createMeasurement(measurement);
            log.debug("Successfully persisted Measurement<UUID='{}'>", measurement.getId());
            responseObserver.onNext(replyBuilder
                .setSuccess(true)
                .setMessage(measurement.getId().toString())
                .build());
            responseObserver.onCompleted();
          } catch (Exception e) {
            log.error("Unable to persist measurement, ERROR: {}", e.toString());
            responseObserver.onNext(replyBuilder
                .setSuccess(false)
                .setMessage("Unable to persist measurement, ERROR: %s".formatted(e.toString()))
                .build());
            responseObserver.onCompleted();
          }
        }
      }
    };
  }
}
