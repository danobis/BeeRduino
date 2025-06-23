package com.daham.graphql.json;

import com.daham.domain.SensorType;
import com.daham.utils.SensorTypeAdapter;
import com.daham.domain.UnitType;
import com.daham.utils.UnitTypeAdapter;
import com.daham.utils.TimestampUtils;
import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.json.bind.annotation.JsonbTypeAdapter;
import lombok.*;
import org.eclipse.microprofile.graphql.Name;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Name("MeasurementOutputJson")
@SuppressWarnings("all")
public class MeasurementOutputJson {
  @JsonbProperty("uuid")
  private UUID id;

  @Setter(AccessLevel.NONE)
  private String timestamp;

  public void setTimestamp(LocalDateTime timestamp) {
    this.timestamp = TimestampUtils.toString(timestamp);
  }

  @JsonbProperty("beehive_uuid")
  private UUID beehiveId;
  private double value;

  @JsonbTypeAdapter(SensorTypeAdapter.class)
  private SensorType type;

  @JsonbTypeAdapter(UnitTypeAdapter.class)
  private UnitType unit;
}
