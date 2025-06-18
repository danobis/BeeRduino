package com.daham.rabbitmq;

import com.daham.core.domain.Measurement;
import com.daham.core.services.MeasurementService;
import com.daham.core.utils.ObjectMapperUtils;
import com.daham.rabbitmq.json.MeasurementInputJson;
import com.rabbitmq.client.*;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.json.bind.Jsonb;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

@Slf4j
@ApplicationScoped
public class MeasurementRabbitmqReceiver implements AutoCloseable {
  private static final String DEFAULT_PROPERTIES_FILE = "/rabbitmq.properties";
  private static final String DEFAULT_RABBITMQ_HOST = "localhost";
  private static final int DEFAULT_RABBITMQ_PORT = 5672;
  private static final String DEFAULT_RABBITMQ_USERNAME = "guest";
  private static final String DEFAULT_RABBITMQ_PASSWORD = "guest";
  private static final String EXCHANGE_NAME = "beehive.measurements";
  private static final String QUEUE_NAME = "measurements.processing.queue";
  private static final String ROUTING_KEY_PATTERN = "beehive.*.#";

  private final Connection connection;
  private final Channel channel;

  @Inject
  MeasurementService measurementService;

  @Inject
  Jsonb jsonb;

  public MeasurementRabbitmqReceiver(String propertiesPath) {
    var properties = new Properties();
    try (var inputStream = MeasurementRabbitmqReceiver.class.getResourceAsStream(propertiesPath)) {
      properties.load(inputStream);
    } catch (Exception e) {
      log.error("Unable to load file '{}', ERROR: {}", propertiesPath, e.toString());
      throw new IllegalStateException(e); // rethrow exception for QuarkusMain
    }
    String rabbitMQHost;
    int rabbitMQPort;
    String rabbitMQUser;
    String rabbitMQPassword;
    ConnectionFactory connectionFactory;
    try {
      /* ---- optional with defaults ------------------------------ */
      rabbitMQHost = properties.getProperty("beehive.service.rabbitmq-host", DEFAULT_RABBITMQ_HOST);
      rabbitMQUser = properties.getProperty("beehive.service.rabbitmq-username", DEFAULT_RABBITMQ_USERNAME);
      rabbitMQPassword = properties.getProperty("beehive.service.rabbitmq-password", DEFAULT_RABBITMQ_PASSWORD);
      var rabbitMQPortValue = properties.getProperty("beehive.service.rabbitmq-port", null);
      /* ---- component setup ------------------------------------- */
      rabbitMQPort = rabbitMQPortValue == null ? DEFAULT_RABBITMQ_PORT : Integer.parseInt(rabbitMQPortValue);
      connectionFactory = new ConnectionFactory();
      connectionFactory.setHost(rabbitMQHost);
      connectionFactory.setPort(rabbitMQPort);
      connectionFactory.setUsername(rabbitMQUser);
      connectionFactory.setPassword(rabbitMQPassword);
      connectionFactory.setAutomaticRecoveryEnabled(true);
      connectionFactory.setNetworkRecoveryInterval(10000);
    } catch (Exception e) {
      log.error("Unable to parse properties in file '{}', ERROR: {}", propertiesPath, e.toString());
      throw new IllegalStateException(e); // rethrow exception for QuarkusMain
    }
    try {
      connection = connectionFactory.newConnection();
      channel = connection.createChannel();
      channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC, true);
      channel.queueDeclare(QUEUE_NAME, true, false, false, null);
      channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, ROUTING_KEY_PATTERN);
    } catch (Exception e) {
      log.error("Unable to initialize RabbitMQ emitter, ERROR: {}", e.toString());
      throw new IllegalStateException(e); // rethrow exception for QuarkusMain
    }
  }

  public MeasurementRabbitmqReceiver() {
    this(DEFAULT_PROPERTIES_FILE);
  }

  public void onStartup(@Observes StartupEvent event) {
    CompletableFuture.runAsync(() -> {
      try {
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
          var envelope = delivery.getEnvelope();
          try {
            var inputJson = new String(delivery.getBody(), StandardCharsets.UTF_8);
            var measurementInput = jsonb.fromJson(inputJson, MeasurementInputJson.class);
            var measurement = ObjectMapperUtils.map(measurementInput, Measurement.class);
            measurementService.createMeasurement(measurement);
            channel.basicAck(envelope.getDeliveryTag(), false);
            log.debug("Successfully persisted Measurement<UUID='{}'>", measurement.getId());
          } catch (Exception e) {
            log.error("Unable to process message body, ERROR: {}", e.toString());
            channel.basicReject(envelope.getDeliveryTag(), false); // reject and requeue
          }
        };
        CancelCallback cancelCallback = channel::basicCancel;
        channel.basicConsume(QUEUE_NAME, false, deliverCallback, cancelCallback);
      } catch (Exception e) {
        log.error("Unable to start RabbitMQ receiver, ERROR: {}", e.toString());
      }
    });
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
  }
}
