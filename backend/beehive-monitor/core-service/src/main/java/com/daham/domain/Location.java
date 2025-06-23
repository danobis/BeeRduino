package com.daham.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "locations")
public class Location extends AbstractEntity {
  @Column(nullable = false)
  private double latitude;

  @Column(nullable = false)
  private double longitude;

  @Column(nullable = false, length = 300)
  private String comment = "";
}
