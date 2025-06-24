package com.daham.producer.simulator.sensors;


import com.daham.producer.domain.SensorType;
import com.daham.producer.domain.UnitType;

import java.util.UUID;

public interface Sensor {
  UUID getSensorId();
  SensorType getSensorType();
  UnitType getUnitType();
  double nextValue();
}
