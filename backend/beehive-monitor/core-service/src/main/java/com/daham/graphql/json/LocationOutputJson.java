package com.daham.graphql.json;

import com.daham.utils.TimestampUtils;
import jakarta.json.bind.annotation.JsonbProperty;
import lombok.*;
import org.eclipse.microprofile.graphql.Name;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Name("LocationOutputJson")
@SuppressWarnings("all")
public class LocationOutputJson {
  @JsonbProperty("uuid")
  private UUID id;

  @Setter(AccessLevel.NONE)
  private String timestamp;

  public void setTimestamp(LocalDateTime timestamp) {
    this.timestamp = TimestampUtils.toString(timestamp);
  }

  private double latitude;
  private double longitude;
  private String comment;
}
