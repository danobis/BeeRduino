package com.daham.core.utils.adapters;

import com.daham.core.domain.SensorType;
import jakarta.json.bind.adapter.JsonbAdapter;

public class SensorTypeAdapter implements JsonbAdapter<SensorType, String> {
  @Override
  public String adaptToJson(SensorType sensorType) {
    return switch (sensorType) {
      case SENSOR_DEFAULT -> "sensor_default";
      case SENSOR_TEMPERATURE_INSIDE -> "sensor_temperature_inside";
      case SENSOR_TEMPERATURE_OUTSIDE -> "sensor_temperature_outside";
      case SENSOR_HUMIDITY_INSIDE -> "sensor_humidity_inside";
      case SENSOR_HUMIDITY_OUTSIDE -> "sensor_humidity_outside";
      case SENSOR_WEIGHT -> "sensor_weight";
    };
  }

  @Override
  public SensorType adaptFromJson(String str) {
    return switch (str) {
      case "sensor_temperature_inside" -> SensorType.SENSOR_TEMPERATURE_INSIDE;
      case "sensor_temperature_outside" -> SensorType.SENSOR_TEMPERATURE_OUTSIDE;
      case "sensor_humidity_inside" -> SensorType.SENSOR_HUMIDITY_INSIDE;
      case "sensor_humidity_outside" -> SensorType.SENSOR_HUMIDITY_OUTSIDE;
      case "sensor_weight" -> SensorType.SENSOR_WEIGHT;
      default -> SensorType.SENSOR_DEFAULT;
    };
  }
}
