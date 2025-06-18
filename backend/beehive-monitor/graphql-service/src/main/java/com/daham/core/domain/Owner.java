package com.daham.core.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "owners")
public class Owner extends AbstractEntity {
  @Column(name = "phone_number",nullable = false, length = 25)
  private String phoneNumber;

  @Column(nullable = false)
  private String email;

  @Column(nullable = false, length = 300)
  private String description = "";

  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
  @Fetch(FetchMode.SELECT)
  @JoinColumn(
    name = "owner_uuid",
    referencedColumnName = "uuid",
    foreignKey = @ForeignKey(name = "fk_beehives_owners"))
  private List<Beehive> beehives = new ArrayList<>();
}
