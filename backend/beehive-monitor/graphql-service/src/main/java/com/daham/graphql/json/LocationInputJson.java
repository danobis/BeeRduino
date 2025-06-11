package com.daham.graphql.json;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.eclipse.microprofile.graphql.Name;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Name("LocationInputJson")
public class LocationInputJson {
  @NotNull(message = "field <latitude> cannot be 'null'")
  @Min(value = -90, message = "<latitude> must be greater than or equal to '-90'")
  @Max(value = 90, message = "<latitude> must be less than or equal to '90'")
  private double latitude;

  @NotNull(message = "field <longitude> cannot be 'null'")
  @Min(value = -180, message = "<longitude> must be greater than or equal to '-180'")
  @Max(value = 180, message = "<longitude> must be less than or equal to '180'")
  private double longitude;
  private String comment;
}
