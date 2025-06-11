package com.daham.client.grpc;

import com.daham.client.RetryDispatcher;
import com.daham.client.model.Measurement;
import com.daham.client.model.SensorType;
import com.daham.client.model.UnitType;
import com.daham.grpc.MeasurementProto;
import com.daham.grpc.MeasurementServiceGrpc;
import com.google.protobuf.Timestamp;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.ZoneId;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
public class MeasurementGrpcClient implements AutoCloseable {
  private final ManagedChannel channel;
  private final MeasurementServiceGrpc.MeasurementServiceStub client;
  private final RetryDispatcher<Measurement> retryDispatcher;

  public MeasurementGrpcClient(String host, int port) {
    try {
      channel = ManagedChannelBuilder
          .forAddress(host, port)
          .usePlaintext()
          .build();
      client = MeasurementServiceGrpc.newStub(channel);
      retryDispatcher = new RetryDispatcher<>(this::sendMeasurementAsync, Duration.ofMinutes(1));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void sendMeasurementAsync(Measurement outputMeasurement) {
    sendMeasurementAsync(outputMeasurement, 1)
        .orTimeout(5, TimeUnit.SECONDS)
        .whenComplete((success, throwable) -> {
          if (throwable != null || !success) {
            retryDispatcher.sendRetry(outputMeasurement);
          }
    });
  }

  private CompletableFuture<Boolean> sendMeasurementAsync(Measurement outputMeasurement, int attempt) {
    CompletableFuture<Boolean> future = new CompletableFuture<>();
    try {
      var response = client.streamMeasurements(new StreamObserver<>() {
        @Override
        public void onNext(MeasurementProto.Reply reply) {
          var success = reply.getSuccess();
          if (success) {
            log.debug("StreamObserver successfully persisted Measurement<UUID='{}'>", reply.getMessage());
          }
          else {
            log.error("StreamObserver server ERROR: {}", reply.getMessage());
          }
          future.complete(success);
        }

        @Override
        public void onError(Throwable throwable) {
          log.error("StreamObserver ERROR: {}", throwable.toString());
          future.completeExceptionally(throwable);
        }

        @Override
        public void onCompleted() {
          log.debug("StreamObserver COMPLETED");
        }
      });
      response.onNext(mapToProtoMeasurement(outputMeasurement));
      response.onCompleted();
    } catch (Exception e) {
      future.completeExceptionally(e);
    }
    return future;
  }

  @Override
  public void close() {
    channel.shutdownNow();
    retryDispatcher.close();
  }

  private MeasurementProto.Measurement mapToProtoMeasurement(Measurement measurement) {
    var timestamp = measurement.getTimestamp()
        .atZone(ZoneId.systemDefault())
        .toInstant();
    return MeasurementProto.Measurement.newBuilder()
        .setTimestamp(Timestamp.newBuilder()
            .setSeconds(timestamp
                .atZone(ZoneId.systemDefault())
                .toEpochSecond())
            .setNanos(timestamp.getNano())
            .build())
        .setOwnerId(measurement.getOwnerId().toString())
        .setBeehiveId(measurement.getBeehiveId().toString())
        .setValue(measurement.getValue())
        .setType(mapToProtoSensorType(measurement.getType()))
        .setUnit(mapToProtoUnitType(measurement.getUnit()))
        .build();
  }

  private MeasurementProto.SensorType mapToProtoSensorType(SensorType type) {
    return switch (type) {
      case SENSOR_DEFAULT -> MeasurementProto.SensorType.SENSOR_DEFAULT;
      case SENSOR_TEMPERATURE_INSIDE -> MeasurementProto.SensorType.SENSOR_TEMPERATURE_INSIDE;
      case SENSOR_TEMPERATURE_OUTSIDE -> MeasurementProto.SensorType.SENSOR_TEMPERATURE_OUTSIDE;
      case SENSOR_HUMIDITY_INSIDE -> MeasurementProto.SensorType.SENSOR_HUMIDITY_INSIDE;
      case SENSOR_HUMIDITY_OUTSIDE -> MeasurementProto.SensorType.SENSOR_HUMIDITY_OUTSIDE;
      case SENSOR_WEIGHT -> MeasurementProto.SensorType.SENSOR_WEIGHT;
    };
  }

  private MeasurementProto.UnitType mapToProtoUnitType(UnitType unit) {
    return switch (unit) {
      case UNIT_DEFAULT -> MeasurementProto.UnitType.UNIT_DEFAULT;
      case UNIT_CELSIUS -> MeasurementProto.UnitType.UNIT_CELSIUS;
      case UNIT_PERCENTAGE -> MeasurementProto.UnitType.UNIT_PERCENTAGE;
      case UNIT_GRAM -> MeasurementProto.UnitType.UNIT_GRAM;
    };
  }
}
