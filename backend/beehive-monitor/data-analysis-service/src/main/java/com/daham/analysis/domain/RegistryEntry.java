package com.daham.analysis.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "beehive_registry")
public class RegistryEntry implements Serializable {
  @Serial
  @Transient
  private static final long serialVersionUID = 1L;

  @Id
  @Column(name = "beehive_uuid", nullable = false)
  private UUID beehiveId;

  @Column(nullable = false)
  private LocalDateTime timestamp;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private StatusType status;

  @Column(nullable = false)
  private boolean deleted = false;
}
