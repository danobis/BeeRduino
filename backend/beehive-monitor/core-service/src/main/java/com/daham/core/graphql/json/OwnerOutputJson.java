package com.daham.core.graphql.json;

import com.daham.common.utils.TimestampUtils;
import jakarta.json.bind.annotation.JsonbProperty;
import lombok.*;
import org.eclipse.microprofile.graphql.Name;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Name("OwnerOutputJson")
@SuppressWarnings("all")
public class OwnerOutputJson {
  @JsonbProperty("uuid")
  private UUID id;

  @Setter(AccessLevel.NONE)
  private String timestamp;

  public void setTimestamp(LocalDateTime timestamp) {
    this.timestamp = TimestampUtils.toString(timestamp);
  }

  @JsonbProperty("phone_number")
  private String phoneNumber;
  private String email;
  private String description;

  @Getter(AccessLevel.NONE)
  private List<BeehiveOutputJson> beehives;

  public List<BeehiveOutputJson> getBeehives() {
    if (beehives != null && !beehives.isEmpty()) {
      return beehives;
    }
    return null;
  }
}
