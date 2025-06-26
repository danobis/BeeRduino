package com.daham.producer.simulator;

import com.daham.producer.domain.Measurement;
import com.daham.producer.domain.SensorType;
import com.daham.producer.services.DataCollectorService;
import com.daham.producer.simulator.sensors.Sensor;
import com.daham.producer.simulator.sensors.SensorFactory;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public final class BeehiveSimulator implements Simulator {
  private static final int DEFAULT_THREAD_POOL_SIZE = 2;

  private final DataCollectorService collectorService;
  private final ScheduledExecutorService executor;
  private final List<Sensor> sensors;

  private volatile boolean isRunning = false;

  public BeehiveSimulator(DataCollectorService collectorService, UUID beehiveId, int threadPoolSize) {
    this.collectorService = collectorService;

    executor = Executors.newScheduledThreadPool(threadPoolSize);
    sensors = List.of(
        SensorFactory.createInsideTemperatureSensor(beehiveId, 33.0, 36.0),
        SensorFactory.createOutsideTemperatureSensor(beehiveId, 10.0, 32.0),
        SensorFactory.createInsideHumiditySensor(beehiveId, 50.0, 70.0),
        SensorFactory.createOutsideHumiditySensor(beehiveId, 30.0, 95.0),
        SensorFactory.createWeightSensor(beehiveId, 11000.0, 45000.0));
  }

  public BeehiveSimulator(DataCollectorService collectorService, UUID beehiveId) {
    this(collectorService, beehiveId, DEFAULT_THREAD_POOL_SIZE);
  }

  @Override
  public void start() {
    if (!isRunning) {
      sensors.forEach(sensor -> {
        if (sensor.getSensorType() != SensorType.DEFAULT) {
          var runnable = new Runnable() {
            @Override
            public void run() {
              try {
                var measurement = Measurement.builder()
                    .timestamp(LocalDateTime.now())
                    .beehiveId(sensor.getSensorId())
                    .value(sensor.nextValue())
                    .type(sensor.getSensorType())
                    .unit(sensor.getUnitType())
                    .build();
                collectorService.publishMeasurement(measurement);
              } catch (Exception e) {
                log.error("Unable to publish measurement, ERROR", e);
                throw new RuntimeException(e);
              }
            }
          };
          if (sensor.getSensorType() == SensorType.WEIGHT) {
            // produce weight measurements every 5 minutes
            executor.scheduleAtFixedRate(runnable, 0, 5, TimeUnit.MINUTES);
          } else {
            // produce temperature and humidity sensor measurements every 5 seconds
            executor.scheduleAtFixedRate(runnable, 0, 5, TimeUnit.SECONDS);
          }
        }
      });
      isRunning = true;
    } else {
      log.warn("Simulator is already running");
    }
  }

  @Override
  public void close() {
    if (isRunning) {
      executor.shutdownNow();
      isRunning = false;
    } else {
      log.warn("Simulator is already stopped");
    }
  }
}
