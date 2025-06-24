package com.daham.core.graphql.json;

import com.daham.common.utils.TimestampUtils;
import com.daham.common.validators.Email;
import com.daham.common.validators.ISOTimestamp;
import com.daham.common.validators.PhoneNumber;
import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.eclipse.microprofile.graphql.Name;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Name("OwnerInputJson")
@SuppressWarnings("all")
public class OwnerInputJson {
  @ISOTimestamp
  @Getter(AccessLevel.NONE)
  private String timestamp;

  public LocalDateTime getTimestamp() {
    if (timestamp != null && !timestamp.isBlank()) {
      return TimestampUtils.fromString(timestamp);
    }
    return null;
  }

  @PhoneNumber
  @NotNull(message = "field <phone_number> cannot be 'null'")
  @NotEmpty(message = "field <phone_number> cannot be 'empty'")
  @JsonbProperty("phone_number")
  private String phoneNumber;

  @Email
  @NotNull(message = "field <email> cannot be 'null'")
  @NotEmpty(message = "field <email> cannot be 'empty'")
  private String email;
  private String description;
}
