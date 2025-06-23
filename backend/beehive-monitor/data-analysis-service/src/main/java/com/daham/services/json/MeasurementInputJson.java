package com.daham.services.json;

import com.daham.domain.SensorType;
import com.daham.domain.UnitType;
import com.daham.utils.SensorTypeAdapter;
import com.daham.utils.UnitTypeAdapter;
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
public class MeasurementInputJson {
  private LocalDateTime timestamp;

  @JsonbProperty("beehive_uuid")
  private UUID beehiveId;
  private double value;

  @JsonbTypeAdapter(SensorTypeAdapter.class)
  private SensorType type;

  @JsonbTypeAdapter(UnitTypeAdapter.class)
  private UnitType unit;
}
