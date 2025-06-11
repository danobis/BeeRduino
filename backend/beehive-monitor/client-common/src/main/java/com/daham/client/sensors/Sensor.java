package com.daham.client.sensors;

import com.daham.client.model.Measurement;
import com.daham.client.model.SensorType;

public interface Sensor {
  SensorType getSensorType();
  Measurement nextMeasurement();
}
