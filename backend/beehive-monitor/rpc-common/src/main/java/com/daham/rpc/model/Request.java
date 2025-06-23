package com.daham.rpc.model;

import jakarta.json.bind.annotation.JsonbProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Request {
  @JsonbProperty(value = "beehive_uuid")
  private UUID beehiveId;
  private LocalDateTime timestamp;
}
