package com.daham.client.sensors;

import com.daham.client.model.SensorType;
import com.daham.client.model.UnitType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SensorFactory {
  public static Sensor createInsideTemperatureSensor(UUID ownerId, UUID beehiveId, double minValue, double maxValue, double maxStepSize) {
    return new SensorImpl(
        ownerId, beehiveId,
        minValue, maxValue, maxStepSize,
        SensorType.SENSOR_TEMPERATURE_INSIDE,
        UnitType.UNIT_CELSIUS);
  }

  public static Sensor createOutsideTemperatureSensor(UUID ownerId, UUID beehiveId, double minValue, double maxValue, double maxStepSize) {
    return new SensorImpl(
        ownerId, beehiveId,
        minValue, maxValue, maxStepSize,
        SensorType.SENSOR_TEMPERATURE_OUTSIDE,
        UnitType.UNIT_CELSIUS);
  }

  public static Sensor createInsideHumiditySensor(UUID ownerId, UUID beehiveId, double minValue, double maxValue, double maxStepSize) {
    return new SensorImpl(
        ownerId, beehiveId,
        minValue, maxValue, maxStepSize,
        SensorType.SENSOR_HUMIDITY_INSIDE,
        UnitType.UNIT_PERCENTAGE);
  }

  public static Sensor createOutsideHumiditySensor(UUID ownerId, UUID beehiveId, double minValue, double maxValue, double maxStepSize) {
    return new SensorImpl(
        ownerId, beehiveId,
        minValue, maxValue, maxStepSize,
        SensorType.SENSOR_HUMIDITY_OUTSIDE,
        UnitType.UNIT_PERCENTAGE);
  }

  public static Sensor createWeightSensor(UUID ownerId, UUID beehiveId, double minValue, double maxValue, double maxStepSize) {
    return new SensorImpl(
        ownerId, beehiveId,
        minValue, maxValue, maxStepSize,
        SensorType.SENSOR_WEIGHT,
        UnitType.UNIT_GRAM);
  }
}
