package com.daham.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@Entity
@Table(name = "measurements")
@NoArgsConstructor
@AllArgsConstructor
public class Measurement {
  @Id
  @GeneratedValue(
      strategy = GenerationType.SEQUENCE,
      generator = "sequence_generator")
  @SequenceGenerator(
      name = "sequence_generator",
      sequenceName = "record_id_sequence",
      allocationSize = 100)
  @Column(name = "record_id", nullable = false)
  private Long recordId;
  private LocalDateTime timestamp;

  @Column(name = "beehive_uuid", nullable = false)
  private UUID beehiveId;

  @Column(name = "measured_value", nullable = false)
  private double value;

  @Column(nullable = false)
  @Enumerated(EnumType.ORDINAL)
  private SensorType type;

  @Column(nullable = false)
  @Enumerated(EnumType.ORDINAL)
  private UnitType unit;
}
