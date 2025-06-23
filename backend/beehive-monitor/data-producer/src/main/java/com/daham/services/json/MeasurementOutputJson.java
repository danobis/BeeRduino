package com.daham.services.json;

import com.daham.domain.SensorType;
import com.daham.domain.UnitType;
import com.daham.utils.SensorTypeAdapter;
import com.daham.utils.UnitTypeAdapter;
import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.json.bind.annotation.JsonbTypeAdapter;
import lombok.*;

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
