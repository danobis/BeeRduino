package com.daham.client.model;

import jakarta.json.bind.annotation.JsonbProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
public class Measurement {
  private LocalDateTime timestamp;

  @JsonbProperty("owner_uuid")
  private UUID ownerId;

  @JsonbProperty("beehive_uuid")
  private UUID beehiveId;
  private double value;
  private SensorType type;
  private UnitType unit;
}
