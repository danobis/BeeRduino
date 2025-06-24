package com.daham.simulator.sensors;

import com.daham.domain.SensorType;
import com.daham.domain.UnitType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SensorFactory {
  public static Sensor createInsideTemperatureSensor(UUID sensorId, double minValue, double maxValue) {
    return new SensorImpl(
        sensorId,
        minValue, maxValue,
        SensorType.TEMPERATURE_INSIDE,
        UnitType.CELSIUS);
  }

  public static Sensor createOutsideTemperatureSensor(UUID sensorId, double minValue, double maxValue) {
    return new SensorImpl(
        sensorId,
        minValue, maxValue,
        SensorType.TEMPERATURE_OUTSIDE,
        UnitType.CELSIUS);
  }

  public static Sensor createInsideHumiditySensor(UUID sensorId, double minValue, double maxValue) {
    return new SensorImpl(
        sensorId,
        minValue, maxValue,
        SensorType.HUMIDITY_INSIDE,
        UnitType.PERCENTAGE);
  }

  public static Sensor createOutsideHumiditySensor(UUID sensorId, double minValue, double maxValue) {
    return new SensorImpl(
        sensorId,
        minValue, maxValue,
        SensorType.HUMIDITY_OUTSIDE,
        UnitType.PERCENTAGE);
  }

  public static Sensor createWeightSensor(UUID sensorId, double minValue, double maxValue) {
    return new SensorImpl(
        sensorId,
        minValue, maxValue,
        SensorType.WEIGHT,
        UnitType.GRAM);
  }
}
