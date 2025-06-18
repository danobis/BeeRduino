package com.daham.graphql.json;

import com.daham.core.domain.SensorType;
import com.daham.core.domain.UnitType;
import com.daham.core.utils.TimestampUtils;
import com.daham.core.utils.adapters.SensorTypeAdapter;
import com.daham.core.utils.adapters.UnitTypeAdapter;
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
public class MeasurementOutputJson {
  @JsonbProperty("uuid")
  private UUID id;

  @Setter(AccessLevel.NONE)
  private String timestamp;

  public void setTimestamp(LocalDateTime timestamp) {
    this.timestamp = TimestampUtils.toString(timestamp);
  }

  @JsonbProperty("owner_uuid")
  private UUID ownerId;

  @JsonbProperty("beehive_uuid")
  private UUID beehiveId;
  private double value;

  @JsonbTypeAdapter(SensorTypeAdapter.class)
  private SensorType type;

  @JsonbTypeAdapter(UnitTypeAdapter.class)
  private UnitType unit;
}
