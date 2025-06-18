package com.daham.rabbitmq.json;

import com.daham.core.domain.SensorType;
import com.daham.core.domain.UnitType;
import com.daham.core.utils.TimestampUtils;
import com.daham.core.utils.adapters.SensorTypeAdapter;
import com.daham.core.utils.adapters.UnitTypeAdapter;
import com.daham.core.utils.validators.ISOTimestamp;
import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.json.bind.annotation.JsonbTypeAdapter;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MeasurementInputJson {
  @ISOTimestamp
  @NotNull(message = "field <timestamp> cannot be 'null'")
  @NotEmpty(message = "field <timestamp> cannot be 'empty'")
  @Getter(AccessLevel.NONE)
  private String timestamp;

  public LocalDateTime getTimestamp() {
    return TimestampUtils.fromString(timestamp);
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
