package com.daham.producer.services.json;

import com.daham.producer.domain.SensorType;
import com.daham.producer.domain.UnitType;
import com.daham.producer.utils.SensorTypeAdapter;
import com.daham.producer.utils.UnitTypeAdapter;
import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.json.bind.annotation.JsonbTypeAdapter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MeasurementOutputJson {
  private LocalDateTime timestamp;

  @JsonbProperty("beehive_uuid")
  private UUID beehiveId;
  private double value;

  @JsonbTypeAdapter(SensorTypeAdapter.class)
  private SensorType type;

  @JsonbTypeAdapter(UnitTypeAdapter.class)
  private UnitType unit;
}
