package com.daham.core.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "beehives")
public class Beehive extends AbstractEntity {
  @Column(name = "owner_uuid", nullable = false)
  private UUID ownerId;

  @Transient
  private UUID locationId; // used for persisting beehives with an existing location

  @Column(nullable = false, length = 300)
  private String comment = "";

  @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
  @Fetch(FetchMode.JOIN)
  @JoinColumn(
    name = "location_uuid",
    referencedColumnName = "uuid",
    nullable = false,
    foreignKey = @ForeignKey(name = "fk_beehives_locations"))
  private Location location;

  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
  @Fetch(FetchMode.SELECT)
  @JoinColumn(
    name = "beehive_uuid",
    referencedColumnName = "uuid",
    foreignKey =  @ForeignKey(name = "fk_measurements_beehives"))
  private List<Measurement> measurements = new ArrayList<>();
}
