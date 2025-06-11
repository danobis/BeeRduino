package com.daham.client.grpc;

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
  private static final String DEFAULT_GRPC_HOST = "localhost";
  private static final int DEFAULT_GRPC_PORT = 9000;
  private static final int DEFAULT_THREAD_POOL_SIZE = 2;

  private final ScheduledExecutorService executor;
  private final MeasurementGrpcClient grpcClient;
  private final List<Sensor> sensors;

  public BeehiveSimulator(String propertiesPath) {
    var properties = new Properties();
    try (var inputStream = BeehiveSimulator.class.getResourceAsStream(propertiesPath)) {
      properties.load(inputStream);
    } catch (Exception e) {
      log.error("Unable to load file '{}', ERROR: {}", propertiesPath, e.toString());
      throw new IllegalStateException(e); // rethrow exception for QuarkusMain
    }
    String grpcHost;
    int grpcPort;
    try {
      /* ---- mandatory fields ------------------------------------ */
      var ownerId = UUID.fromString(getRequiredProperty(properties, "beehive.simulator.owner-uuid"));
      var beehiveId = UUID.fromString(getRequiredProperty(properties, "beehive.simulator.beehive-uuid"));
      /* ---- optional with defaults ------------------------------ */
      grpcHost = properties.getProperty("beehive.simulator.grpc-host", DEFAULT_GRPC_HOST);
      var grpcPortValue = properties.getProperty("beehive.simulator.grpc-port", null);
      var threadPoolSizeValue = properties.getProperty("beehive.simulator.thread-pool.size", null);
      /* ---- component setup ------------------------------------- */
      grpcPort = grpcPortValue == null ? DEFAULT_GRPC_PORT : Integer.parseInt(grpcPortValue);
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
      grpcClient = new MeasurementGrpcClient(grpcHost, grpcPort);
    } catch (Exception e) {
      log.error("Unable to initialize gRPC Client, ERROR: {}", e.toString());
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
            grpcClient.sendMeasurementAsync(sensor.nextMeasurement());
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
    grpcClient.close();
  }

  private String getRequiredProperty(Properties properties, String key) {
    var value = properties.getProperty(key, null);
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("Property '%s' expected, value is null or empty".formatted(key));
    }
    return value;
  }
}
