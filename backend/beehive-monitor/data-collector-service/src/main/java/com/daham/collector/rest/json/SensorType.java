package com.daham.collector.rest.json;

import com.daham.collector.utils.SensorTypeAdapter;
import jakarta.json.bind.annotation.JsonbTypeAdapter;

@JsonbTypeAdapter(SensorTypeAdapter.class)
public enum SensorType {
  DEFAULT,
  TEMPERATURE_INSIDE,
  TEMPERATURE_OUTSIDE,
  HUMIDITY_INSIDE,
  HUMIDITY_OUTSIDE,
  WEIGHT,
}
