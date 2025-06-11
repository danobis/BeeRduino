package com.daham.core.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "measurements")
public class Measurement extends AbstractEntity {
  @Column(name = "owner_uuid", nullable = false)
  private UUID ownerId;

  @Column(name = "beehive_uuid", nullable = false)
  private UUID beehiveId;

  @Column(nullable = false)
  private double value;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private SensorType type;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private UnitType unit;
}
