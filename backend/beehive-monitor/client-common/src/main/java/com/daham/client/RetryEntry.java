package com.daham.client;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class RetryEntry<T> {
  private T payload;
  private int attempt;

  public void incrementAttempt() {
    attempt++;
  }
}
