package com.daham.graphql.json;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.eclipse.microprofile.graphql.Name;
import org.hibernate.validator.constraints.Range;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Name("LocationInputJson")
public class LocationInputJson {
  @NotNull(message = "field <latitude> cannot be 'null'")
  @Range(min = -90, max = 90, message = "<latitude> must be between '-90' and '90'")
  private double latitude;

  @NotNull(message = "field <longitude> cannot be 'null'")
  @Range(min = -180, max = 180, message = "<longitude> must be between '-180' and '180'")
  private double longitude;
  private String comment;
}
