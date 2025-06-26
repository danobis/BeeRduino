package com.daham.core.graphql.json;

import com.daham.common.utils.TimestampUtils;
import com.daham.common.validators.ISOTimestamp;
import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.eclipse.microprofile.graphql.Name;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@OneOfLocation
@Name("BeehiveInputJson")
@SuppressWarnings("all")
public class BeehiveInputJson {
  @ISOTimestamp
  @Getter(AccessLevel.NONE)
  private String timestamp;

  public LocalDateTime getTimestamp() {
    if (timestamp != null && !timestamp.isBlank()) {
      return TimestampUtils.fromString(timestamp);
    }
    return null;
  }

  @Valid
  @JsonbProperty("location")
  private LocationInputJson location;

  @JsonbProperty("location_uuid")
  private UUID locationId;

  @JsonbProperty("owner_uuid")
  @NotNull(message = "field <owner_uuid> cannot be 'null'")
  private UUID ownerId;
  private String comment;
}
