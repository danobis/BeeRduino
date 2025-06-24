package com.daham.simulator.sensors;

import com.daham.domain.SensorType;
import com.daham.domain.UnitType;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public final class SensorImpl implements Sensor {
  private final UUID sensorId;
  private final double minValue;
  private final double maxValue;
  private final SensorType sensorType;
  private final UnitType unitType;

  private double previousValue;

  public SensorImpl(UUID sensorId, double minValue, double maxValue, SensorType sensorType, UnitType unitType) {
    this.sensorId = sensorId;
    this.minValue = minValue;
    this.maxValue = maxValue;
    this.sensorType = sensorType;
    this.unitType = unitType;
  }

  @Override
  public UUID getSensorId() {
    return sensorId;
  }

  @Override
  public SensorType getSensorType() {
    return sensorType;
  }

  @Override
  public UnitType getUnitType() {
    return unitType;
  }

  @Override
  public double nextValue() {
    final Random generator = ThreadLocalRandom.current();
    final LocalDateTime now = LocalDateTime.now();
    double hour = now.getHour();
    double normalizedHour = hour / 24.0;
    double nextValue = Double.NaN;
    if (sensorType == SensorType.TEMPERATURE_INSIDE) {
      double base = (minValue + maxValue) / 2.0;
      double noise = generator.nextGaussian() * 0.3;
      nextValue = Math.clamp(base + noise, minValue, maxValue);
    }
    if (sensorType == SensorType.TEMPERATURE_OUTSIDE) {
      double base = (minValue + maxValue) / 2.0;
      double amplitude = (maxValue - minValue) / 2.0;
      double noise = generator.nextGaussian() * 0.5;
      double t = base + amplitude * Math.sin(2 * Math.PI * (normalizedHour - 0.4));
      nextValue = Math.clamp(t + noise, minValue, maxValue);
    }
    if (sensorType == SensorType.HUMIDITY_INSIDE) {
      double base = (minValue + maxValue) / 2.0;
      double noise = generator.nextGaussian() * 2.0;
      nextValue = Math.clamp(base + noise, minValue, maxValue);
    }
    if (sensorType == SensorType.HUMIDITY_OUTSIDE) {
      double base = (minValue + maxValue) / 2.0;
      double amplitude = (maxValue - minValue) / 2.0;
      double h = base - amplitude * Math.sin(2 * Math.PI * (normalizedHour - 0.2));
      double noise = generator.nextGaussian();
      nextValue = Math.clamp(h + noise, minValue, maxValue);
    }
    if (sensorType == SensorType.WEIGHT) {
      if (Double.isNaN(previousValue)) {
        previousValue = minValue;
      }
      double step;
      double hourOfDay = now.getHour();
      if (hourOfDay >= 6 && hourOfDay <= 20) {
        double baseStep = 20.0;
        double variation = generator.nextGaussian() * 5.0;
        step = baseStep + variation;
      }
      else {
        step = generator.nextDouble(0.0, 5.0) * -1;
      }
      double w = previousValue + step;
      nextValue = Math.clamp(w, minValue, maxValue);
      previousValue = nextValue;
    }
    return nextValue;
  }
}
