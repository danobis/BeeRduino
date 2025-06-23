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
@Name("BeehiveOutputJson")
@SuppressWarnings("all")
public class BeehiveOutputJson {
  @JsonbProperty("uuid")
  private UUID id;

  @Setter(AccessLevel.NONE)
  private String timestamp;

  public void setTimestamp(LocalDateTime timestamp) {
    this.timestamp = TimestampUtils.toString(timestamp);
  }

  private LocationOutputJson location;
  private OwnerOutputJson owner;

  @Getter(AccessLevel.NONE)
  @JsonbProperty("owner_uuid")
  private UUID ownerId;

  public UUID getOwnerId() {
    if (owner != null) {
      return null;
    }
    return ownerId;
  }

  private String comment;
}
