package com.daham.client.sensors;

import com.daham.client.model.Measurement;
import com.daham.client.model.SensorType;
import com.daham.client.model.UnitType;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@AllArgsConstructor
public final class SensorImpl implements Sensor {
  private static double previousValue = Double.NaN;

  private final UUID ownerId;
  private final UUID beehiveId;
  private final double minValue;
  private final double maxValue;
  private final double maxStepSize;
  private final SensorType sensorType;
  private final UnitType unitType;

  @Override
  public SensorType getSensorType() {
    return sensorType;
  }

  @Override
  public Measurement nextMeasurement() {
    final Random generator = ThreadLocalRandom.current();
    if (Double.isNaN(previousValue)) {
      previousValue = generator.nextDouble(minValue, maxValue);
    }
    var delta = generator.nextDouble(1, maxStepSize) * (generator.nextBoolean() ?  1 : -1);
    var nextValue = Math.max(minValue, Math.min(maxValue, previousValue + delta));
    var measurement = Measurement.builder()
        .timestamp(LocalDateTime.now())
        .ownerId(ownerId)
        .beehiveId(beehiveId)
        .value(nextValue)
        .type(sensorType)
        .unit(unitType)
        .build();
    previousValue = nextValue;
    return measurement;
  }
}
