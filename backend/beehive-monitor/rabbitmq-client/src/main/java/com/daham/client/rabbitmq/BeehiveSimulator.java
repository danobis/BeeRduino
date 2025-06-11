package com.daham.client.rabbitmq;

import com.daham.client.model.SensorType;
import com.daham.client.sensors.Sensor;
import com.daham.client.sensors.SensorFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@SuppressWarnings("all")
public final class BeehiveSimulator implements AutoCloseable {
  private static final String DEFAULT_PROPERTIES_FILE = "/beehives/beehive.properties";
  private static final String DEFAULT_RABBITMQ_HOST = "localhost";
  private static final int DEFAULT_RABBITMQ_PORT = 5672;
  private static final String DEFAULT_RABBITMQ_USERNAME = "guest";
  private static final String DEFAULT_RABBITMQ_PASSWORD = "guest";
  private static final int DEFAULT_THREAD_POOL_SIZE = 2;

  private final ScheduledExecutorService executor;
  private final MeasurementRabbitmqEmitter rabbitmqClient;
  private final List<Sensor> sensors;

  public BeehiveSimulator(String propertiesPath) {
    var properties = new Properties();
    try (var inputStream = BeehiveSimulator.class.getResourceAsStream(propertiesPath)) {
      properties.load(inputStream);
    } catch (Exception e) {
      log.error("Unable to load file '{}', ERROR: {}", propertiesPath, e.toString());
      throw new IllegalStateException(e); // rethrow exception for QuarkusMain
    }
    String rabbitMQHost;
    int rabbitMQPort;
    String rabbitMQUser;
    String rabbitMQPassword;
    try {
      /* ---- mandatory fields ------------------------------------ */
      var ownerId = UUID.fromString(getRequiredProperty(properties, "beehive.simulator.owner-uuid"));
      var beehiveId = UUID.fromString(getRequiredProperty(properties, "beehive.simulator.beehive-uuid"));
      /* ---- optional with defaults ------------------------------ */
      rabbitMQHost = properties.getProperty("beehive.simulator.rabbitmq-host", DEFAULT_RABBITMQ_HOST);
      rabbitMQUser = properties.getProperty("beehive.simulator.rabbitmq-username", DEFAULT_RABBITMQ_USERNAME);
      rabbitMQPassword = properties.getProperty("beehive.simulator.rabbitmq-password", DEFAULT_RABBITMQ_PASSWORD);
      var rabbitMQPortValue = properties.getProperty("beehive.simulator.rabbitmq-port", null);
      var threadPoolSizeValue = properties.getProperty("beehive.simulator.thread-pool.size", null);
      /* ---- component setup ------------------------------------- */
      rabbitMQPort = rabbitMQPortValue == null ? DEFAULT_RABBITMQ_PORT : Integer.parseInt(rabbitMQPortValue);
      var threadPoolSize = threadPoolSizeValue == null ? DEFAULT_THREAD_POOL_SIZE : Integer.parseInt(threadPoolSizeValue);
      executor = Executors.newScheduledThreadPool(threadPoolSize);
      sensors = List.of(
          SensorFactory.createInsideTemperatureSensor(ownerId, beehiveId, 20.0, 40.0, 5.0),
          SensorFactory.createOutsideTemperatureSensor(ownerId, beehiveId, -5.0, 35.0, 10.0),
          SensorFactory.createInsideHumiditySensor(ownerId, beehiveId, 40.0, 80.0, 2.5),
          SensorFactory.createOutsideHumiditySensor(ownerId, beehiveId, 30.0, 100.0, 10.0),
          SensorFactory.createWeightSensor(ownerId, beehiveId, 9500.0, 45000.0, 100.0));
    } catch (Exception e) {
      log.error("Unable to parse properties in file '{}', ERROR: {}", propertiesPath, e.toString());
      throw new IllegalStateException(e); // rethrow exception for QuarkusMain
    }
    try {
      rabbitmqClient = new MeasurementRabbitmqEmitter(rabbitMQHost, rabbitMQPort, rabbitMQUser, rabbitMQPassword);
    } catch (Exception e) {
      log.error("Unable to initialize RabbitMQ emitter, ERROR: {}", e.toString());
      throw new IllegalStateException(e); // rethrow exception for QuarkusMain
    }
  }

  public BeehiveSimulator() {
    this(DEFAULT_PROPERTIES_FILE);
  }

  public void start() {
    sensors.forEach(sensor -> {
      if (sensor.getSensorType() != SensorType.SENSOR_DEFAULT) {
        var runnable = new Runnable() {
          @Override
          public void run() {
            rabbitmqClient.sendMeasurementAsync(sensor.nextMeasurement());
          }
        };
        if (sensor.getSensorType() == SensorType.SENSOR_WEIGHT) {
          // produce weight measurements every 5 minutes
          executor.scheduleAtFixedRate(runnable, 0, 5, TimeUnit.MINUTES);
        }
        else {
          // produce temperature and humidity sensor measurements every 5 seconds
          executor.scheduleAtFixedRate(runnable, 0, 5, TimeUnit.SECONDS);
        }
      }
    });
  }

  @Override
  public void close() {
    executor.shutdownNow();
    rabbitmqClient.close();
  }

  private String getRequiredProperty(Properties properties, String key) {
    var value = properties.getProperty(key, null);
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("Property '%s' expected, value is null or empty".formatted(key));
    }
    return value;
  }
}
