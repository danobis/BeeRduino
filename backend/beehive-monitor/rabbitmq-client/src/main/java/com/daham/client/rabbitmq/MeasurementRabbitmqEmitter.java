package com.daham.client.rabbitmq;

import com.daham.client.RetryDispatcher;
import com.daham.client.model.Measurement;
import com.daham.client.utils.JsonUtils;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
public class MeasurementRabbitmqEmitter implements AutoCloseable {
  private static final String EXCHANGE_NAME = "beehive.measurements";

  private final Connection connection;
  private final Channel channel;
  private final RetryDispatcher<Measurement> retryDispatcher;

  public MeasurementRabbitmqEmitter(String rabbitMQHost, int rabbitMQPort, String rabbitMQUser, String rabbitMQPassword) {
    try {
      var connectionFactory = new ConnectionFactory();
      connectionFactory.setHost(rabbitMQHost);
      connectionFactory.setPort(rabbitMQPort);
      connectionFactory.setUsername(rabbitMQUser);
      connectionFactory.setPassword(rabbitMQPassword);
      connectionFactory.setAutomaticRecoveryEnabled(true);
      connectionFactory.setNetworkRecoveryInterval(10000);
      connection = connectionFactory.newConnection();
      channel = connection.createChannel();
      channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC, true);
      retryDispatcher = new RetryDispatcher<>(this::sendMeasurementAsync, Duration.ofMinutes(1));
    } catch (IOException | TimeoutException e) {
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

  private CompletableFuture<Boolean> sendMeasurementAsync(Measurement measurement, int attempt) {
    CompletableFuture<Boolean> future = new CompletableFuture<>();
    try {
      var routingKey = "beehive.%s.%s".formatted(measurement.getBeehiveId(), measurement.getType());
      var messageValue = JsonUtils.toJsonString(measurement);
      channel.basicPublish(EXCHANGE_NAME, routingKey, null, messageValue.getBytes());
      log.debug("Successfully enqueued Measurement<ROUTING_KEY={}> to RabbitMQ", routingKey);
      future.complete(true);
    } catch (Exception e) {
      future.completeExceptionally(e);
    }
    return future;
  }

  @Override
  public void close() {
    try {
      if (channel != null && channel.isOpen()) {
        channel.close();
      }
    } catch (IOException | TimeoutException e) {
      log.error("Unable to close RabbitMQ channel, ERROR: {}", e.toString());
      throw new RuntimeException(e);
    }
    try {
      if (connection != null && connection.isOpen()) {
        connection.close();
      }
    } catch (IOException e) {
      log.error("Unable to close RabbitMQ connection, ERROR: {}", e.toString());
      throw new RuntimeException(e);
    }
    retryDispatcher.close();
  }
}
