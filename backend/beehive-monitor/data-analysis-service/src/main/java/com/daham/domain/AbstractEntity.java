package com.daham.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@MappedSuperclass
public abstract class AbstractEntity implements Serializable {
  @Serial
  @Transient
  private static final long serialVersionUID = 1L;

  @Id @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "uuid", nullable = false, updatable = false)
  protected UUID id;

  @Column(nullable = false)
  protected LocalDateTime timestamp;
}
