package com.daham.rest.json;

import com.daham.utils.TimestampUtils;
import com.daham.utils.validators.ISOTimestamp;
import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("all")
public class MeasurementJson {
  @ISOTimestamp
  @NotNull(message = "field <timestamp> cannot be 'null'")
  @Getter(AccessLevel.NONE)
  private String timestamp;

  public LocalDateTime getTimestamp() {
    return TimestampUtils.fromString(timestamp);
  }

  @JsonbProperty("beehive_uuid")
  private UUID beehiveId;
  private double value;
  private SensorType type;
  private UnitType unit;
}
