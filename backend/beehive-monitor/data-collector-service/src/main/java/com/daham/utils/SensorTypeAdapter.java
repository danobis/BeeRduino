package com.daham.utils;

import com.daham.rest.json.SensorType;
import jakarta.json.bind.adapter.JsonbAdapter;

public class SensorTypeAdapter implements JsonbAdapter<SensorType, String> {
  @Override
  public String adaptToJson(SensorType sensorType) {
    return switch (sensorType) {
      case DEFAULT -> "sensor_default";
      case TEMPERATURE_INSIDE -> "sensor_temperature_inside";
      case TEMPERATURE_OUTSIDE -> "sensor_temperature_outside";
      case HUMIDITY_INSIDE -> "sensor_humidity_inside";
      case HUMIDITY_OUTSIDE -> "sensor_humidity_outside";
      case WEIGHT -> "sensor_weight";
    };
  }

  @Override
  public SensorType adaptFromJson(String str) {
    return switch (str) {
      case "sensor_temperature_inside" -> SensorType.TEMPERATURE_INSIDE;
      case "sensor_temperature_outside" -> SensorType.TEMPERATURE_OUTSIDE;
      case "sensor_humidity_inside" -> SensorType.HUMIDITY_INSIDE;
      case "sensor_humidity_outside" -> SensorType.HUMIDITY_OUTSIDE;
      case "sensor_weight" -> SensorType.WEIGHT;
      default -> SensorType.DEFAULT;
    };
  }
}
