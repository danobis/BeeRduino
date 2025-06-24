package com.daham.analysis.services.json;

import com.daham.analysis.domain.SensorType;
import com.daham.analysis.domain.UnitType;
import com.daham.analysis.utils.SensorTypeAdapter;
import com.daham.analysis.utils.UnitTypeAdapter;
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
