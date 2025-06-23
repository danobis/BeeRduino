package com.daham.simulator.sensors;


import com.daham.domain.SensorType;
import com.daham.domain.UnitType;

import java.util.UUID;

public interface Sensor {
  UUID getSensorId();
  SensorType getSensorType();
  UnitType getUnitType();
  double nextValue();
}
