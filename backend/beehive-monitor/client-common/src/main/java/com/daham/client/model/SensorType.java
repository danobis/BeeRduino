package com.daham.client.model;

import com.daham.client.utils.adapters.SensorTypeAdapter;
import jakarta.json.bind.annotation.JsonbTypeAdapter;

@JsonbTypeAdapter(SensorTypeAdapter.class)
public enum SensorType {
  SENSOR_DEFAULT,
  SENSOR_TEMPERATURE_INSIDE,
  SENSOR_TEMPERATURE_OUTSIDE,
  SENSOR_HUMIDITY_INSIDE,
  SENSOR_HUMIDITY_OUTSIDE,
  SENSOR_WEIGHT,
}
